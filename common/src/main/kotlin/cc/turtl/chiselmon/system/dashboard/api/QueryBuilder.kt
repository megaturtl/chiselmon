package cc.turtl.chiselmon.system.dashboard.api

import java.sql.Connection
import java.sql.ResultSet

/**
 * Lightweight fluent SQL query builder for SELECT statements against the encounters table.
 *
 * Constructed via [ApiHandler.query]. Handlers chain conditions, ordering, and limits via the
 * fluent API. Time range filtering is applied by chaining [timeRange], which inserts the
 * appropriate `encountered_ms` conditions alongside any other handler-specific clauses.
 *
 * Example usage in a handler:
 * ```
 * query("encounters")
 *     .timeRange(timeRange)
 *     .select("species, COUNT(*) as cnt")
 *     .where("is_shiny = TRUE")
 *     .groupBy("species")
 *     .orderBy("cnt DESC")
 *     .limit(20)
 *     .fetchList { rs -> SpeciesEntry(rs.getString("species"), rs.getLong("cnt")) }
 * ```
 */
class QueryBuilder internal constructor(
    private val conn: Connection,
    private val table: String,
) {

    private var select: String = "*"
    private val conditions = mutableListOf<String>()
    private var groupBy: String? = null
    private var orderBy: String? = null
    private var limit: Int? = null

    /**
     * Applies a [TimeRange] filter to this query.
     * Adds `encountered_ms >= from` and/or `encountered_ms <= to` conditions as appropriate.
     * Has no effect if the range is unbounded.
     */
    fun timeRange(timeRange: TimeRange): QueryBuilder = apply {
        if (timeRange.hasFrom) where("encountered_ms >= ${timeRange.from}")
        if (timeRange.hasTo) where("encountered_ms <= ${timeRange.to}")
    }

    /** Columns or expressions to SELECT. Defaults to `*`. */
    fun select(columns: String): QueryBuilder = apply { this.select = columns }

    /** Appends a raw WHERE condition (joined with AND). */
    fun where(condition: String): QueryBuilder = apply { conditions.add(condition) }

    fun groupBy(column: String): QueryBuilder = apply { this.groupBy = column }

    fun orderBy(clause: String): QueryBuilder = apply { this.orderBy = clause }

    fun limit(n: Int): QueryBuilder = apply { this.limit = n }

    /** Builds and executes the query, mapping each row via [mapper]. */
    fun <T> fetchList(mapper: (ResultSet) -> T): List<T> {
        val results = mutableListOf<T>()
        conn.prepareStatement(buildSql()).use { ps ->
            ps.executeQuery().use { rs ->
                while (rs.next()) results.add(mapper(rs))
            }
        }
        return results
    }

    /**
     * Builds and executes the query, returning the first row mapped by [mapper],
     * or null if no rows were returned.
     */
    fun <T> fetchOne(mapper: (ResultSet) -> T): T? {
        conn.prepareStatement(buildSql()).use { ps ->
            ps.executeQuery().use { rs ->
                return if (rs.next()) mapper(rs) else null
            }
        }
    }

    /**
     * Builds and executes a `SELECT COUNT(*)` query using the current conditions,
     * ignoring any select/groupBy/orderBy/limit set on this builder.
     */
    fun fetchCount(): Long {
        val sql = buildString {
            append("SELECT COUNT(*) FROM ").append(table)
            appendWhere(this)
        }
        conn.prepareStatement(sql).use { ps ->
            ps.executeQuery().use { rs ->
                return if (rs.next()) rs.getLong(1) else 0L
            }
        }
    }

    /**
     * Builds and executes the query, returning a flat interleaved int array:
     * `[colA_row0, colB_row0, colA_row1, colB_row1, ...]`.
     *
     * More compact than a list of objects for coordinate data — eliminates repeated field
     * names in the JSON serialization.
     */
    fun fetchInterleavedPairs(colA: String, colB: String): IntArray {
        val buf = mutableListOf<Int>()
        conn.prepareStatement(buildSql()).use { ps ->
            ps.executeQuery().use { rs ->
                while (rs.next()) {
                    buf.add(rs.getInt(colA))
                    buf.add(rs.getInt(colB))
                }
            }
        }
        return buf.toIntArray()
    }

    private fun buildSql(): String = buildString {
        append("SELECT ").append(select).append(" FROM ").append(table)
        appendWhere(this)
        groupBy?.let { append(" GROUP BY ").append(it) }
        orderBy?.let { append(" ORDER BY ").append(it) }
        limit?.let { append(" LIMIT ").append(it) }
    }

    private fun appendWhere(sb: StringBuilder) {
        if (conditions.isNotEmpty()) sb.append(" WHERE ").append(conditions.joinToString(" AND "))
    }
}
