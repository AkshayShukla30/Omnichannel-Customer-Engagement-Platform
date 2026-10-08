package com.omnichannel.platform.reporting;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class ReportingRepository {

    private final JdbcTemplate jdbc;

    public ReportingRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<HandleTimeRow> avgHandleTime(LocalDateTime from, LocalDateTime to) {
        return jdbc.query("CALL sp_avg_handle_time(?, ?)",
                (rs, i) -> new HandleTimeRow(rs.getString("skill"), rs.getLong("interactions"), rs.getDouble("avg_seconds")),
                from, to);
    }

    public List<AgentLoadRow> interactionsPerAgent(LocalDateTime from, LocalDateTime to) {
        return jdbc.query("CALL sp_interactions_per_agent(?, ?)",
                (rs, i) -> new AgentLoadRow(rs.getString("agent"), rs.getLong("interactions")), from, to);
    }

    public List<QueueRow> queueLength() {
        return jdbc.query("CALL sp_queue_length()", (rs, i) -> new QueueRow(rs.getString("skill"), rs.getLong("waiting")));
    }

    public List<DispositionRow> dispositionBreakdown(LocalDateTime from, LocalDateTime to) {
        return jdbc.query("CALL sp_disposition_breakdown(?, ?)",
                (rs, i) -> new DispositionRow(rs.getString("disposition"), rs.getLong("total")), from, to);
    }

    public record HandleTimeRow(String skill, long interactions, double avgSeconds) {
    }

    public record AgentLoadRow(String agent, long interactions) {
    }

    public record QueueRow(String skill, long waiting) {
    }

    public record DispositionRow(String disposition, long total) {
    }
}
