package com.omnichannel.platform.agent;

import com.omnichannel.platform.common.BusinessRuleException;
import com.omnichannel.platform.common.NotFoundException;
import com.omnichannel.platform.common.UnauthorizedException;
import com.omnichannel.platform.routing.RoutingEngine;
import java.time.Clock;
import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@Service
public class AgentService {

    private static final Logger log = LoggerFactory.getLogger(AgentService.class);

    private final AgentRepository agents;
    private final PasswordEncoder passwordEncoder;
    private final RoutingEngine routing;
    private final Clock clock;
    private final TransactionTemplate tx;

    public AgentService(AgentRepository agents, PasswordEncoder passwordEncoder, RoutingEngine routing, Clock clock,
            PlatformTransactionManager transactionManager) {
        this.agents = agents;
        this.passwordEncoder = passwordEncoder;
        this.routing = routing;
        this.clock = clock;
        this.tx = new TransactionTemplate(transactionManager);
    }

    public Agent login(String username, String password) {
        Agent agent = agents.findByUsername(username)
                .filter(a -> passwordEncoder.matches(password, a.getPasswordHash()))
                .orElseThrow(() -> new UnauthorizedException("Invalid username or password"));

        LocalDateTime now = LocalDateTime.now(clock);
        Agent updated = tx.execute(status -> {
            Agent managed = agents.findById(agent.getId()).orElseThrow();
            managed.setLastSeenAt(now);
            if (managed.getStatus() == AgentStatus.OFFLINE) {
                managed.markAvailable(now);
            }
            return managed;
        });
        log.info("Agent {} logged in", username);
        routing.dispatch();
        return updated;
    }

    public Agent changeStatus(Long agentId, AgentStatus target) {
        if (target == AgentStatus.BUSY) {
            throw new BusinessRuleException("BUSY is set by the routing engine, choose AVAILABLE or OFFLINE");
        }
        Agent updated = tx.execute(status -> {
            Agent agent = agents.findById(agentId).orElseThrow(() -> new NotFoundException("Agent not found"));
            if (agent.getStatus() == AgentStatus.BUSY) {
                throw new BusinessRuleException("Close your current interaction before changing status");
            }
            if (target == AgentStatus.AVAILABLE) {
                agent.markAvailable(LocalDateTime.now(clock));
                agent.setLastSeenAt(LocalDateTime.now(clock));
            } else {
                agent.markOffline();
            }
            return agent;
        });
        if (target == AgentStatus.AVAILABLE) {
            routing.dispatch();
        }
        return updated;
    }

    public Agent get(Long agentId) {
        return agents.findById(agentId).orElseThrow(() -> new NotFoundException("Agent not found"));
    }
}
