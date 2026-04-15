package cc.turtl.chiselmon.system.dashboard.api.endpoints

import cc.turtl.chiselmon.system.dashboard.api.ApiHandler
import cc.turtl.chiselmon.system.tracker.EncounterDatabase
import com.sun.net.httpserver.HttpExchange

class TimelineHandler(db: EncounterDatabase) : ApiHandler(db) {

    private data class TimelineBucket(val bucket: Long, val count: Long)
    private data class TimelineResponse(val granularity: String, val bucketMs: Long, val buckets: List<TimelineBucket>)

    override fun handle(exchange: HttpExchange) {
        handleRequest(exchange) { timeRange, params ->
            // ?granularity=minute|hour (default: hour)
            val granularity = params["granularity"] ?: "hour"
            val isMinute = granularity.equals("minute", ignoreCase = true)
            val bucketMs = if (isMinute) 60_000L else 3_600_000L

            val buckets = query("encounters").timeRange(timeRange)
                .select("FLOOR(encountered_ms / $bucketMs) * $bucketMs AS bucket, COUNT(*) AS cnt")
                .groupBy("bucket")
                .orderBy("bucket ASC")
                .fetchList { rs -> TimelineBucket(rs.getLong("bucket"), rs.getLong("cnt")) }

            TimelineResponse(granularity, bucketMs, buckets)
        }
    }
}
