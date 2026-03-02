package cc.turtl.chiselmon.system.dashboard.api.endpoints;

import cc.turtl.chiselmon.system.dashboard.api.ApiHandler;
import cc.turtl.chiselmon.system.tracker.EncounterDatabase;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.util.List;

public class TimelineHandler extends ApiHandler {

    private record TimelineBucket(long bucket, long count) {
    }

    private record TimelineResponse(String granularity, long bucketMs, List<TimelineBucket> buckets) {
    }

    public TimelineHandler(EncounterDatabase db) {
        super(db);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        handleRequest(exchange, (timeRange, params) -> {
            // ?granularity=minute|hour (default: hour)
            String granularity = params.getOrDefault("granularity", "hour");
            boolean isMinute = "minute".equalsIgnoreCase(granularity);
            long bucketMs = isMinute ? 60_000L : 3_600_000L;

            List<TimelineBucket> buckets = query("encounters").timeRange(timeRange)
                    .select("FLOOR(encountered_ms / " + bucketMs + ") * " + bucketMs + " AS bucket, COUNT(*) AS cnt")
                    .groupBy("bucket")
                    .orderBy("bucket ASC")
                    .fetchList(rs -> new TimelineBucket(rs.getLong("bucket"), rs.getLong("cnt")));

            return new TimelineResponse(granularity, bucketMs, buckets);
        });
    }
}