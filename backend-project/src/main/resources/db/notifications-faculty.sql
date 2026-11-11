ALTER TABLE notifications
    MODIFY COLUMN student_id BIGINT NULL,
    ADD COLUMN faculty_id BIGINT NULL AFTER student_id,
    ADD INDEX idx_notifications_faculty_created (faculty_id, created_at);
