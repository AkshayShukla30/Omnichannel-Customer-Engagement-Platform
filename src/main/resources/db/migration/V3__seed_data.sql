INSERT INTO agents (username, password_hash, full_name, phone, status) VALUES
('priya',  '$2b$10$6cN2OjijaQWOO1Vz0/akx.xQa4sS17.zGrWPrbqwGzjK7KeWX1xGC', 'Priya Nair',    NULL, 'OFFLINE'),
('rahul',  '$2b$10$6cN2OjijaQWOO1Vz0/akx.xQa4sS17.zGrWPrbqwGzjK7KeWX1xGC', 'Rahul Menon',   NULL, 'OFFLINE'),
('ananya', '$2b$10$6cN2OjijaQWOO1Vz0/akx.xQa4sS17.zGrWPrbqwGzjK7KeWX1xGC', 'Ananya Das',    NULL, 'OFFLINE'),
('vikram', '$2b$10$6cN2OjijaQWOO1Vz0/akx.xQa4sS17.zGrWPrbqwGzjK7KeWX1xGC', 'Vikram Rao',    NULL, 'OFFLINE'),
('meera',  '$2b$10$6cN2OjijaQWOO1Vz0/akx.xQa4sS17.zGrWPrbqwGzjK7KeWX1xGC', 'Meera Pillai',  NULL, 'OFFLINE');

INSERT INTO agent_skills (agent_id, skill)
SELECT a.id, s.skill FROM agents a JOIN (
    SELECT 'priya' AS u, 'LOAN' AS skill UNION ALL SELECT 'priya', 'GENERAL'
    UNION ALL SELECT 'rahul', 'CARD' UNION ALL SELECT 'rahul', 'GENERAL'
    UNION ALL SELECT 'ananya', 'COMPLAINT' UNION ALL SELECT 'ananya', 'CARD' UNION ALL SELECT 'ananya', 'GENERAL'
    UNION ALL SELECT 'vikram', 'LOAN' UNION ALL SELECT 'vikram', 'CARD' UNION ALL SELECT 'vikram', 'COMPLAINT' UNION ALL SELECT 'vikram', 'GENERAL'
    UNION ALL SELECT 'meera', 'GENERAL' UNION ALL SELECT 'meera', 'COMPLAINT'
) s ON s.u = a.username;

INSERT INTO core_banking_customers (customer_id, full_name, email, phone, segment, account_number, account_balance, active_loans, card_type) VALUES
('CUST-1001', 'Aarav Sharma', 'aarav.sharma@example.com', '+919800010001', 'PREMIER', '50100000011', 125430.50, 1, 'PLATINUM'),
('CUST-1002', 'Diya Verma', 'diya.verma@example.com', '+919800010002', 'STANDARD', '50100000021', 18250.00, 0, 'CLASSIC'),
('CUST-1003', 'Rohan Mehta', 'rohan.mehta@example.com', '+919800010003', 'PREMIER', '50100000031', 342900.75, 2, 'SIGNATURE'),
('CUST-1004', 'Ishita Nair', 'ishita.nair@example.com', '+919800010004', 'STANDARD', '50100000041', 9420.10, 1, 'CLASSIC'),
('CUST-1005', 'Kabir Singh', 'kabir.singh@example.com', '+919800010005', 'BASIC', '50100000051', 2150.00, 0, 'NONE'),
('CUST-1006', 'Ananya Iyer', 'ananya.iyer@example.com', '+919800010006', 'PREMIER', '50100000061', 88760.25, 0, 'PLATINUM'),
('CUST-1007', 'Vivaan Gupta', 'vivaan.gupta@example.com', '+919800010007', 'STANDARD', '50100000071', 41300.00, 3, 'CLASSIC'),
('CUST-1008', 'Saanvi Reddy', 'saanvi.reddy@example.com', '+919800010008', 'BASIC', '50100000081', 760.40, 0, 'NONE'),
('CUST-1009', 'Arjun Kapoor', 'arjun.kapoor@example.com', '+919800010009', 'PREMIER', '50100000091', 215000.00, 1, 'SIGNATURE'),
('CUST-1010', 'Meera Joshi', 'meera.joshi@example.com', '+919800010010', 'STANDARD', '50100000101', 30210.90, 2, 'CLASSIC');

-- two weeks of closed history so the dashboard is not empty on first start
INSERT INTO interactions (channel, skill, priority, customer_ref, status, assigned_agent_id, created_at, assigned_at, closed_at, disposition)
WITH RECURSIVE seq (n) AS (
    SELECT 1 UNION ALL SELECT n + 1 FROM seq WHERE n < 80
),
timed AS (
    SELECT n,
           NOW() - INTERVAL (n * 4 + 1) HOUR AS created,
           (n * 7) % 100 + 20  AS wait_s,
           (n * 53) % 840 + 60 AS handle_s
    FROM seq
)
SELECT IF(n % 3 = 0, 'CHAT', 'CALL'),
       ELT(1 + n % 4, 'LOAN', 'CARD', 'COMPLAINT', 'GENERAL'),
       IF(n % 9 = 0, 1, 2),
       CONCAT('CUST-', 1001 + n % 10),
       'CLOSED',
       (SELECT MIN(id) FROM agents) + ELT(1 + n % 7, 0, 0, 1, 2, 3, 4, 0),
       created,
       created + INTERVAL wait_s SECOND,
       created + INTERVAL (wait_s + handle_s) SECOND,
       ELT(1 + n % 8, 'RESOLVED', 'RESOLVED', 'RESOLVED', 'FOLLOW_UP_REQUIRED', 'FOLLOW_UP_REQUIRED', 'ESCALATED', 'CALLBACK_REQUESTED', 'CUSTOMER_DISCONNECTED')
FROM timed;
