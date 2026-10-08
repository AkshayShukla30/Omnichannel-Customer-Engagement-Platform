package com.omnichannel.platform.reporting;

import static org.assertj.core.api.Assertions.assertThat;

import com.omnichannel.platform.AbstractMySqlIntegrationTest;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class ReportingIntegrationTest extends AbstractMySqlIntegrationTest {

    @Autowired
    ReportingRepository reports;

    private final LocalDateTime from = LocalDateTime.now().minusDays(60);
    private final LocalDateTime to = LocalDateTime.now().plusMinutes(1);

    @Test
    void storedProceduresReturnTheSeededHistory() {
        assertThat(reports.avgHandleTime(from, to)).isNotEmpty()
                .allSatisfy(row -> assertThat(row.avgSeconds()).isPositive());
        assertThat(reports.interactionsPerAgent(from, to)).hasSizeGreaterThanOrEqualTo(5);
        assertThat(reports.dispositionBreakdown(from, to)).extracting(ReportingRepository.DispositionRow::disposition)
                .contains("RESOLVED", "ESCALATED");
    }

    @Test
    void queueLengthAlwaysListsAllFourSkills() {
        assertThat(reports.queueLength()).extracting(ReportingRepository.QueueRow::skill)
                .containsExactly("LOAN", "CARD", "COMPLAINT", "GENERAL");
    }
}
