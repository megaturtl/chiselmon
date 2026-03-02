package cc.turtl.chiselmon.system.dashboard.api;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Lightweight fluent SQL query builder for SELECT statements against the encounters table.
 * <p>
 * Constructed via {@link ApiHandler#query(String)}. Handlers chain conditions,
 * ordering, and limits via the fluent API. Time range filtering is applied by
 * chaining {@link #timeRange(TimeRange)}, which inserts the appropriate
 * {@code encountered_ms} conditions alongside any other handler-specific clauses.
 * <p>
 * Example usage in a handler:
 * <pre>{@code
 * return query("encounters")
 *         .timeRange(timeRange)
 *         .select("species, COUNT(*) as cnt")
 *         .where("is_shiny = TRUE")
 *         .groupBy("species")
 *         .orderBy("cnt DESC")
 *         .limit(20)
 *         .fetchList(rs -> new SpeciesEntry(rs.getString("species"), rs.getLong("cnt")));
 * }</pre>
 */
public class QueryBuilder {

    @FunctionalInterface
    public interface RowMapper<T> {
        T map(ResultSet rs) throws SQLException;
    }

    private final Connection conn;
    private final String table;

    private String select = "*";
    private final List<String> conditions = new ArrayList<>();
    private String groupBy;
    private String orderBy;
    private Integer limit;

    QueryBuilder(Connection conn, String table) {
        this.conn = conn;
        this.table = table;
    }

    /**
     * Applies a {@link TimeRange} filter to this query.
     * Adds {@code encountered_ms >= from} and/or {@code encountered_ms <= to}
     * conditions as appropriate. Has no effect if the range is unbounded.
     */
    public QueryBuilder timeRange(TimeRange timeRange) {
        if (timeRange.hasFrom()) where("encountered_ms >= " + timeRange.from());
        if (timeRange.hasTo()) where("encountered_ms <= " + timeRange.to());
        return this;
    }

    /**
     * Columns or expressions to SELECT. Defaults to {@code *}.
     */
    public QueryBuilder select(String columns) {
        this.select = columns;
        return this;
    }

    /** Appends a raw WHERE condition (joined with AND). */
    public QueryBuilder where(String condition) {
        conditions.add(condition);
        return this;
    }

    public QueryBuilder groupBy(String column) {
        this.groupBy = column;
        return this;
    }

    public QueryBuilder orderBy(String clause) {
        this.orderBy = clause;
        return this;
    }

    public QueryBuilder limit(int n) {
        this.limit = n;
        return this;
    }

    /** Builds and executes the query, mapping each row with the provided {@link RowMapper}. */
    public <T> List<T> fetchList(RowMapper<T> mapper) throws SQLException {
        List<T> results = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(buildSql());
             ResultSet rs = ps.executeQuery()) {
            while (rs.next())
                results.add(mapper.map(rs));
        }
        return results;
    }

    /**
     * Builds and executes the query, returning the first row mapped by the provided
     * {@link RowMapper}, or {@code null} if no rows were returned.
     */
    public <T> T fetchOne(RowMapper<T> mapper) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(buildSql());
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? mapper.map(rs) : null;
        }
    }

    /**
     * Builds and executes a {@code SELECT COUNT(*)} query using the current conditions,
     * ignoring any select/groupBy/orderBy/limit set on this builder.
     */
    public long fetchCount() throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM ").append(table);
        appendWhere(sql);
        try (PreparedStatement ps = conn.prepareStatement(sql.toString());
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getLong(1) : 0;
        }
    }


    /**
     * Builds and executes the query, returning a flat interleaved int array:
     * {@code [colA_row0, colB_row0, colA_row1, colB_row1, ...]}.
     * <p>
     * More compact than a list of objects for coordinate data — eliminates
     * repeated field names in the JSON serialization.
     *
     * @param colA first column name (e.g. {@code "pokemon_x"})
     * @param colB second column name (e.g. {@code "pokemon_z"})
     * @return interleaved int array
     */
    public int[] fetchInterleavedPairs(String colA, String colB) throws SQLException {
        List<Integer> buf = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(buildSql());
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                buf.add(rs.getInt(colA));
                buf.add(rs.getInt(colB));
            }
        }
        int[] result = new int[buf.size()];
        for (int i = 0; i < buf.size(); i++) result[i] = buf.get(i);
        return result;
    }

    private String buildSql() {
        StringBuilder sql = new StringBuilder("SELECT ").append(select)
                .append(" FROM ").append(table);
        appendWhere(sql);
        if (groupBy != null) sql.append(" GROUP BY ").append(groupBy);
        if (orderBy != null) sql.append(" ORDER BY ").append(orderBy);
        if (limit != null) sql.append(" LIMIT ").append(limit);
        return sql.toString();
    }

    private void appendWhere(StringBuilder sql) {
        if (!conditions.isEmpty())
            sql.append(" WHERE ").append(String.join(" AND ", conditions));
    }
}