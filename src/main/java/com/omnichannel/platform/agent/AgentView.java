package com.omnichannel.platform.agent;

import com.omnichannel.platform.common.Skill;
import java.util.Set;

public record AgentView(Long id, String username, String fullName, AgentStatus status, Set<Skill> skills) {

    public static AgentView from(Agent agent) {
        return new AgentView(agent.getId(), agent.getUsername(), agent.getFullName(), agent.getStatus(), agent.getSkills());
    }
}
