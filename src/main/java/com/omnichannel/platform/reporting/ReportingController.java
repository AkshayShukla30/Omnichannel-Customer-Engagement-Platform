package com.omnichannel.platform.reporting;

import com.omnichannel.platform.reporting.ReportingRepository.AgentLoadRow;
import com.omnichannel.platform.reporting.ReportingRepository.DispositionRow;
import com.omnichannel.platform.reporting.ReportingRepository.HandleTimeRow;
import com.omnichannel.platform.reporting.ReportingRepository.QueueRow;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports")
@Tag(name = "Reporting", description = "Dashboard metrics, each one is backed by a MySQL stored procedure")
public class ReportingController {

    private final ReportingRepository reports;
    private final Clock clock;

    public ReportingController(ReportingRepository reports, Clock clock) {
        this.reports = reports;
        this.clock = clock;
    }

    @GetMapping("/handle-time")
    @Operation(summary = "Average handle time in seconds per skill (sp_avg_handle_time)")
    public List<HandleTimeRow> handleTime(@RequestParam(defaultValue = "30") @Min(1) @Max(365) int days) {
        return reports.avgHandleTime(since(days), until());
    }

    @GetMapping("/interactions-per-agent")
    @Operation(summary = "Closed interactions per agent (sp_interactions_per_agent)")
    public List<AgentLoadRow> perAgent(@RequestParam(defaultValue = "30") @Min(1) @Max(365) int days) {
        return reports.interactionsPerAgent(since(days), until());
    }

    @GetMapping("/queue-length")
    @Operation(summary = "Interactions waiting right now per skill (sp_queue_length)")
    public List<QueueRow> queueLength() {
        return reports.queueLength();
    }

    @GetMapping("/dispositions")
    @Operation(summary = "Closed interactions by disposition (sp_disposition_breakdown)")
    public List<DispositionRow> dispositions(@RequestParam(defaultValue = "30") @Min(1) @Max(365) int days) {
        return reports.dispositionBreakdown(since(days), until());
    }

    private LocalDateTime since(int days) {
        return LocalDateTime.now(clock).minusDays(days);
    }

    // DATETIME columns round to the second, without the cushion an interaction closed a moment ago can fall outside the range
    private LocalDateTime until() {
        return LocalDateTime.now(clock).plusSeconds(2);
    }
}
