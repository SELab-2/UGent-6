-- The table creation statements you provided go here, unchanged.
-- Skipping this part for brevity as you've already provided it above.

-- Inserting into `users`
INSERT INTO users (name, surname, email, azure_id, role) VALUES
('John', 'Doe', 'john.doe@example.com', 'token_1', 'student'),
('Jane', 'Smith', 'jane.smith@example.com', 'token_2', 'teacher'),
('Bob', 'Brown', 'bob.brown@example.com', 'token_3', 'admin'),
('Alice', 'Johnson', 'alice.johnson@example.com', 'token_4', 'student'),
('Charlie', 'Davis', 'charlie.davis@example.com', 'token_5', 'teacher');

-- Inserting into `courses`
INSERT INTO courses (course_name, description) VALUES
('Math 101', 'Introduction to Mathematics'),
('Science 101', 'Basics of Scientific Method'),
('History 101', 'World History Overview'),
('Computer Science 101', 'Introduction to Computing'),
('English 101', 'English Literature');

-- Inserting into `course_users`
-- Assume course_id and user_id start from 1 and match accordingly
INSERT INTO course_users (course_id, user_id, course_relation) VALUES
(1, 1, 'enrolled'),
(2, 1, 'enrolled'),
(3, 2, 'creator'),
(4, 3, 'course_admin'),
(5, 4, 'enrolled');

-- Inserting into `files`
-- Assume files are uploaded by different users
INSERT INTO files (file_path, file_name, uploaded_by) VALUES
('/path/to/file1', 'file1.txt', 1),
('/path/to/file2', 'file2.txt', 2),
('/path/to/file3', 'file3.txt', 3),
('/path/to/file4', 'file4.txt', 4),
('/path/to/file5', 'file5.txt', 1),
('/path/to/file6', 'file6.txt', 2),
('/path/to/file7', 'file7.txt', 3),
('/path/to/file8', 'file8.txt', 4),
('/path/to/file9', 'file9.txt', 1),
('/path/to/file10', 'file10.txt', 2),
('/path/to/file11', 'file11.txt', 3),
('/path/to/file12', 'file12.txt', 4),
('/path/to/file13', 'file13.txt', 5),
('/path/to/file14', 'file14.txt', 4),
('/path/to/file15', 'file15.txt', 5),
('/path/to/file16', 'file16.txt', 1),
('/path/to/file17', 'file17.txt', 2),
('/path/to/file18', 'file18.txt', 3),
('/path/to/file19', 'file19.txt', 4),
('/path/to/file20', 'file20.txt', 1),
('/path/to/file21', 'file21.txt', 2),
('/path/to/file22', 'file22.txt', 3);

-- Assume tests are created before projects for foreign key constraints
-- Inserting into `tests`
INSERT INTO tests (docker_image, docker_test, structure_test_id) VALUES
('docker/image1', 2, 3),
('docker/image2', 5, 6),
('docker/image3', 8, 9),
('docker/image4', 12, 13),
('docker/image5', 14, 15);

-- Inserting into `group_clusters`
INSERT INTO group_clusters (course_id, cluster_name, max_size, group_amount) VALUES
(1, 'Project: priemgetallen', 4, 20),
(2, 'Analyse van alkanen', 3, 10),
(3, 'Groepswerk industriële revolutie', 5, 13),
(4, 'Linux practica', 2, 100),
(5, 'Review: A shaskespeare story', 3, 30);

-- Inserting into `groups`
INSERT INTO groups (group_name, group_cluster) VALUES
('Group 1', 1),
('Group 2', 2),
('Group 3', 3),
('Group 4', 4),
('Group 5', 5);

-- Inserting into `group_users`
-- Linking users to groups, assuming group_id and user_id start from 1
INSERT INTO group_users (group_id, user_id) VALUES
(1, 1),
(2, 2),
(3, 3),
(4, 4),
(5, 5);

INSERT INTO projects (course_id, test_id, project_name, description, group_cluster_id, max_score, deadline)
VALUES
    (1, 1, 'Math project 1', 'Solve equations', 1, 20, '2024-03-20 09:00'),
    (2, 2, 'Science Lab 1', 'Conduct experiment', 2, 20, '2024-03-21 15:30'),
    (3, 3, 'History Essay 1', 'Discuss historical event', 3, NULL, '2024-03-22 12:00'),
    (4, 4, 'Programming Assignment 1', 'Write code', 4, 4, '2024-03-23 14:45'),
    (5, 5, 'Literature Analysis', 'Analyze text', 5, 10, '2024-03-24 10:00');





-- Inserting into `solutions`
-- Linking solutions to projects and groups
INSERT INTO submissions (project_id, group_id, file_id, structure_accepted, docker_accepted, structure_feedback_fileid, docker_feedback_fileid, submission_time)
VALUES
    (1, 1, 16, true, true, 17, 18, CURRENT_TIMESTAMP),
    (2, 2, 17, false, false, 18, 19, CURRENT_TIMESTAMP),
    (3, 3, 18, false, false, 19, 20, CURRENT_TIMESTAMP),
    (4, 4, 19, true, true, 20, 21, CURRENT_TIMESTAMP),
    (5, 5, 20, true, true, 21, 22, CURRENT_TIMESTAMP);

-- Inserting into `group_grades`
-- Assign grades to group solutions
INSERT INTO group_feedback(group_id, project_id, grade, feedback) VALUES
(1, 1, 95.0, ''),
(2, 2, 88.5, ''),
(3, 3, NULL, ''),
(4, 4, 89.0, ''),
(5, 5, 94.5, '');
