package com.omnichannel.platform.agent;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "interaction_notes")
public class InteractionNote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "interaction_id", nullable = false)
    private Long interactionId;

    @Column(name = "agent_id", nullable = false)
    private Long agentId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String body;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    protected InteractionNote() {
    }

    public InteractionNote(Long interactionId, Long agentId, String body, LocalDateTime createdAt) {
        this.interactionId = interactionId;
        this.agentId = agentId;
        this.body = body;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public String getBody() {
        return body;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
