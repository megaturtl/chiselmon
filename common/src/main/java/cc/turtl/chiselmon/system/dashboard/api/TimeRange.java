package cc.turtl.chiselmon.system.dashboard.api;

import java.util.Map;

/**
 * Represents an optional time range filter derived from {@code from} and {@code to}
 * query parameters (both epoch milliseconds).
 * <p>
 * Either bound may be absent (0 = unset = open-ended).
 * <p>
 * Example URLs:
 * <ul>
 *   <li>{@code /api/biomes} — all time</li>
 *   <li>{@code /api/biomes?from=1700000000000} — from timestamp onwards</li>
 *   <li>{@code /api/biomes?from=1700000000000&to=1710000000000} — bounded range</li>
 * </ul>
 */
public record TimeRange(long from, long to) {

    public boolean hasFrom() {
        return from > 0;
    }

    public boolean hasTo() {
        return to > 0;
    }

    public boolean isUnbounded() {
        return !hasFrom() && !hasTo();
    }

    /**
     * Appends the appropriate {@code WHERE} or {@code AND} conditions for this
     * time range to an existing SQL query string.
     * <p>
     * Safe to call when the range is unbounded — the original SQL is returned unchanged.
     *
     * @param sql a complete SQL statement, possibly already containing a WHERE clause
     * @return the SQL with time range conditions appended
     */
    public String applyTo(String sql) {
        if (isUnbounded()) return sql;

        StringBuilder sb = new StringBuilder(sql);
        boolean hasWhere = sql.toUpperCase().contains("WHERE");

        if (hasFrom()) {
            sb.append(hasWhere ? " AND" : " WHERE")
                    .append(" encountered_ms >= ").append(from);
            hasWhere = true;
        }
        if (hasTo()) {
            sb.append(hasWhere ? " AND" : " WHERE")
                    .append(" encountered_ms <= ").append(to);
        }
        return sb.toString();
    }

    /**
     * Parses a {@link TimeRange} from the query parameters of an HTTP request.
     * Missing or non-numeric values default to {@code 0} (unset).
     */
    public static TimeRange from(Map<String, String> params) {
        long from = parseLong(params.get("from"));
        long to = parseLong(params.get("to"));
        return new TimeRange(from, to);
    }

    private static long parseLong(String value) {
        if (value == null) return 0;
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}