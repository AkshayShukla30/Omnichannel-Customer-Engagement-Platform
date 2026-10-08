package com.omnichannel.platform.agent;

import com.omnichannel.platform.common.Skill;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface AgentRepository extends JpaRepository<Agent, Long> {

    Optional<Agent> findByUsername(String username);

    @Query("select distinct a from Agent a join a.skills s "
            + "where a.status = :status and s = :skill and a.lastSeenAt >= :seenAfter")
    List<Agent> findReadyWithSkill(@Param("status") AgentStatus status, @Param("skill") Skill skill,
            @Param("seenAfter") LocalDateTime seenAfter);

    @Transactional
    @Modifying
    @Query("update Agent a set a.lastSeenAt = :now where a.id = :id")
    void touch(@Param("id") Long id, @Param("now") LocalDateTime now);
}
