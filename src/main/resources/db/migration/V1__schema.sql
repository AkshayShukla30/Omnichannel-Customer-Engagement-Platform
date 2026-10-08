CREATE TABLE agents (
    id                BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    username          VARCHAR(50)  NOT NULL UNIQUE,
    password_hash     VARCHAR(100) NOT NULL,
    full_name         VARCHAR(100) NOT NULL,
    phone             VARCHAR(20)  NULL,
    status            VARCHAR(20)  NOT NULL DEFAULT 'OFFLINE',
    idle_since        DATETIME     NULL,
    last_seen_at      DATETIME     NULL
);

CREATE TABLE agent_skills (
    agent_id BIGINT      NOT NULL,
    skill    VARCHAR(20) NOT NULL,
    PRIMARY KEY (agent_id, skill),
    CONSTRAINT fk_agent_skills_agent FOREIGN KEY (agent_id) REFERENCES agents (id)
);

CREATE TABLE interactions (
    id                BIGINT      NOT NULL AUTO_INCREMENT PRIMARY KEY,
    channel           VARCHAR(10) NOT NULL,
    skill             VARCHAR(20) NOT NULL,
    priority          INT         NOT NULL DEFAULT 2,
    customer_ref      VARCHAR(50) NOT NULL,
    external_ref      VARCHAR(64) NULL,
    status            VARCHAR(10) NOT NULL DEFAULT 'QUEUED',
    assigned_agent_id BIGINT      NULL,
    created_at        DATETIME    NOT NULL,
    assigned_at       DATETIME    NULL,
    closed_at         DATETIME    NULL,
    disposition       VARCHAR(30) NULL,
    CONSTRAINT fk_interactions_agent FOREIGN KEY (assigned_agent_id) REFERENCES agents (id)
);

CREATE INDEX idx_interactions_queue  ON interactions (status, priority, created_at);
CREATE INDEX idx_interactions_closed ON interactions (status, closed_at);
CREATE INDEX idx_interactions_agent  ON interactions (assigned_agent_id, assigned_at);

CREATE TABLE interaction_notes (
    id             BIGINT   NOT NULL AUTO_INCREMENT PRIMARY KEY,
    interaction_id BIGINT   NOT NULL,
    agent_id       BIGINT   NOT NULL,
    body           TEXT     NOT NULL,
    created_at     DATETIME NOT NULL,
    CONSTRAINT fk_notes_interaction FOREIGN KEY (interaction_id) REFERENCES interactions (id),
    CONSTRAINT fk_notes_agent FOREIGN KEY (agent_id) REFERENCES agents (id)
);

CREATE TABLE core_banking_customers (
    customer_id     VARCHAR(50)    NOT NULL PRIMARY KEY,
    full_name       VARCHAR(100)   NOT NULL,
    email           VARCHAR(120)   NULL,
    phone           VARCHAR(20)    NULL,
    segment         VARCHAR(20)    NOT NULL,
    account_number  VARCHAR(20)    NOT NULL,
    account_balance DECIMAL(15, 2) NOT NULL,
    active_loans    INT            NOT NULL DEFAULT 0,
    card_type       VARCHAR(20)    NOT NULL DEFAULT 'NONE'
);
