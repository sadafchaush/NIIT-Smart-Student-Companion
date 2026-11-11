CREATE TABLE notifications (
    id BIGINT NOT NULL AUTO_INCREMENT,
    student_id BIGINT NULL,
    faculty_id BIGINT NULL,
    title VARCHAR(200) NOT NULL,
    message VARCHAR(2000) NOT NULL,
    type VARCHAR(32) NOT NULL,
    target_url VARCHAR(255) NOT NULL,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME NOT NULL,
    PRIMARY KEY (id),
    INDEX idx_notifications_student_created (student_id, created_at),
    INDEX idx_notifications_faculty_created (faculty_id, created_at)
);
