package com.omnichannel.platform.routing;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InteractionRepository extends JpaRepository<Interaction, Long> {

    List<Interaction> findByStatusOrderByPriorityAscCreatedAtAsc(InteractionStatus status);

    Optional<Interaction> findFirstByAssignedAgentIdAndStatus(Long agentId, InteractionStatus status);

    long countByAssignedAgentIdAndAssignedAtAfter(Long agentId, LocalDateTime since);
}
