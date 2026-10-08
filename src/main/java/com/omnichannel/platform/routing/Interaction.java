package com.omnichannel.platform.routing;

import com.omnichannel.platform.agent.Disposition;
import com.omnichannel.platform.common.Channel;
import com.omnichannel.platform.common.Skill;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "interactions")
public class Interaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Channel channel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Skill skill;

    @Column(nullable = false)
    private int priority;

    @Column(name = "customer_ref", nullable = false)
    private String customerRef;

    @Column(name = "external_ref")
    private String externalRef;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InteractionStatus status = InteractionStatus.QUEUED;

    @Column(name = "assigned_agent_id")
    private Long assignedAgentId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "assigned_at")
    private LocalDateTime assignedAt;

    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    @Enumerated(EnumType.STRING)
    private Disposition disposition;

    protected Interaction() {
    }

    public Interaction(Channel channel, Skill skill, int priority, String customerRef, String externalRef,
            LocalDateTime createdAt) {
        this.channel = channel;
        this.skill = skill;
        this.priority = priority;
        this.customerRef = customerRef;
        this.externalRef = externalRef;
        this.createdAt = createdAt;
    }

    public void assignTo(Long agentId, LocalDateTime now) {
        status = InteractionStatus.ASSIGNED;
        assignedAgentId = agentId;
        assignedAt = now;
    }

    public void close(Disposition disposition, LocalDateTime now) {
        status = InteractionStatus.CLOSED;
        this.disposition = disposition;
        closedAt = now;
    }

    public Long getId() {
        return id;
    }

    public Channel getChannel() {
        return channel;
    }

    public Skill getSkill() {
        return skill;
    }

    public int getPriority() {
        return priority;
    }

    public String getCustomerRef() {
        return customerRef;
    }

    public String getExternalRef() {
        return externalRef;
    }

    public InteractionStatus getStatus() {
        return status;
    }

    public Long getAssignedAgentId() {
        return assignedAgentId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getAssignedAt() {
        return assignedAt;
    }

    public LocalDateTime getClosedAt() {
        return closedAt;
    }

    public Disposition getDisposition() {
        return disposition;
    }
}
