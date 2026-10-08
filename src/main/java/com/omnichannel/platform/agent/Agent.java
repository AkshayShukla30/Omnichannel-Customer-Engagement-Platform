package com.omnichannel.platform.agent;

import com.omnichannel.platform.common.Skill;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import org.hibernate.annotations.DynamicUpdate;

@Entity
@DynamicUpdate
@Table(name = "agents")
public class Agent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    private String phone;

    @Enumerated(EnumType.STRING)
    private AgentStatus status = AgentStatus.OFFLINE;

    @Column(name = "idle_since")
    private LocalDateTime idleSince;

    @Column(name = "last_seen_at")
    private LocalDateTime lastSeenAt;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "agent_skills", joinColumns = @JoinColumn(name = "agent_id"))
    @Column(name = "skill")
    @Enumerated(EnumType.STRING)
    private Set<Skill> skills = new HashSet<>();

    protected Agent() {
    }

    public Agent(String username, String fullName, Set<Skill> skills) {
        this.username = username;
        this.fullName = fullName;
        this.skills = new HashSet<>(skills);
    }

    public void markAvailable(LocalDateTime now) {
        status = AgentStatus.AVAILABLE;
        idleSince = now;
    }

    public void markBusy() {
        status = AgentStatus.BUSY;
        idleSince = null;
    }

    public void markOffline() {
        status = AgentStatus.OFFLINE;
        idleSince = null;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getFullName() {
        return fullName;
    }

    public String getPhone() {
        return phone;
    }

    public AgentStatus getStatus() {
        return status;
    }

    public LocalDateTime getIdleSince() {
        return idleSince;
    }

    public LocalDateTime getLastSeenAt() {
        return lastSeenAt;
    }

    public void setLastSeenAt(LocalDateTime lastSeenAt) {
        this.lastSeenAt = lastSeenAt;
    }

    public Set<Skill> getSkills() {
        return skills;
    }
}
