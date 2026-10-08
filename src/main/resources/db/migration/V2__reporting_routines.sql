DELIMITER //

-- no stored function on purpose: creating one needs SUPER (or log_bin_trust_function_creators) for non-root users
CREATE PROCEDURE sp_avg_handle_time(IN p_from DATETIME, IN p_to DATETIME)
BEGIN
    SELECT skill,
           COUNT(*) AS interactions,
           ROUND(AVG(TIMESTAMPDIFF(SECOND, assigned_at, closed_at)), 1) AS avg_seconds
    FROM interactions
    WHERE status = 'CLOSED' AND closed_at BETWEEN p_from AND p_to
    GROUP BY skill
    ORDER BY skill;
END //

CREATE PROCEDURE sp_interactions_per_agent(IN p_from DATETIME, IN p_to DATETIME)
BEGIN
    SELECT a.full_name AS agent, COUNT(i.id) AS interactions
    FROM agents a
    LEFT JOIN interactions i
           ON i.assigned_agent_id = a.id
          AND i.status = 'CLOSED'
          AND i.closed_at BETWEEN p_from AND p_to
    GROUP BY a.id, a.full_name
    ORDER BY interactions DESC, a.full_name;
END //

CREATE PROCEDURE sp_queue_length()
BEGIN
    SELECT s.skill, COUNT(i.id) AS waiting
    FROM (SELECT 'LOAN' AS skill UNION ALL SELECT 'CARD' UNION ALL SELECT 'COMPLAINT' UNION ALL SELECT 'GENERAL') s
    LEFT JOIN interactions i ON i.skill = s.skill AND i.status = 'QUEUED'
    GROUP BY s.skill
    ORDER BY FIELD(s.skill, 'LOAN', 'CARD', 'COMPLAINT', 'GENERAL');
END //

CREATE PROCEDURE sp_disposition_breakdown(IN p_from DATETIME, IN p_to DATETIME)
BEGIN
    SELECT disposition, COUNT(*) AS total
    FROM interactions
    WHERE status = 'CLOSED' AND closed_at BETWEEN p_from AND p_to
    GROUP BY disposition
    ORDER BY total DESC, disposition;
END //

DELIMITER ;
