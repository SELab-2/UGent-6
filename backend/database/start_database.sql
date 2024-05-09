DROP SCHEMA public CASCADE;
CREATE SCHEMA public;

-- Users table to store information about users
CREATE TABLE users (
    user_id SERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    surname VARCHAR(50) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    azure_id VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Courses table to store information about courses
CREATE TABLE courses (
    course_id SERIAL PRIMARY KEY,
    course_name VARCHAR(100) NOT NULL,
    description TEXT,
    course_year INTEGER,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    archived_at TIMESTAMP WITH TIME ZONE DEFAULT NULL,
    join_key TEXT
);

-- Linking table to associate users with courses and define their role in the course
CREATE TABLE course_users (
    course_id INT REFERENCES courses(course_id),
    user_id INT REFERENCES users(user_id),
    course_relation VARCHAR(50) NOT NULL,
    PRIMARY KEY (course_id, user_id)
);

CREATE TABLE group_clusters (
    group_cluster_id SERIAL PRIMARY KEY,
    course_id INT REFERENCES courses(course_id),
    max_size INT NOT NULL,
    cluster_name VARCHAR(100) NOT NULL,
    group_amount INT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Files table to store file information
CREATE TABLE files (
    file_id SERIAL PRIMARY KEY,
    file_path VARCHAR(512) NOT NULL,
    file_name VARCHAR(512) NOT NULL,
    uploaded_by INT REFERENCES users(user_id)
);

-- A id for the docker test and an id for the file test id
-- docker test is enabled if script is not null
-- docker test is in simple mode if template is null
CREATE TABLE tests (
    test_id SERIAL PRIMARY KEY,
    docker_image VARCHAR(256),
    docker_test_script TEXT,
    docker_test_template TEXT,
    structure_template TEXT
);



-- projects table to store information about projects within a course
-- test_id points to internal test, possibly a docker or a file structure test.

CREATE TABLE projects (
    project_id SERIAL PRIMARY KEY,
    course_id INT REFERENCES courses(course_id),
    project_name VARCHAR(100) NOT NULL,
    description TEXT,
    group_cluster_id INT REFERENCES group_clusters(group_cluster_id),
    deadline TIMESTAMP WITH TIME ZONE NOT NULL,
    test_id INT REFERENCES tests(test_id),
    visible BOOLEAN DEFAULT false NOT NULL,
    max_score INT
);



-- Groups table to manage groups of students
CREATE TABLE groups (
    group_id SERIAL PRIMARY KEY,
    group_name VARCHAR(100) NOT NULL,
    group_cluster INT REFERENCES group_clusters(group_cluster_id)
);

-- Group grades table to store grades for groups in projects
CREATE TABLE group_feedback (
    group_id INT REFERENCES groups(group_id),
    project_id INT REFERENCES projects(project_id),
    grade FLOAT,
    feedback TEXT,
    PRIMARY KEY (group_id, project_id)
);



-- Solutions table to store student or group submissions for projects
-- Solo projects are done with group clusters with 1 person.

CREATE TABLE submissions (
    submission_id SERIAL PRIMARY KEY,
    project_id INT REFERENCES projects(project_id),
    group_id INT REFERENCES groups(group_id),
    file_id INT REFERENCES files(file_id),
    structure_accepted BOOLEAN NOT NULL,
    docker_accepted BOOLEAN NOT NULL,
    structure_feedback TEXT,
    docker_feedback TEXT,
    docker_test_state VARCHAR(10) DEFAULT "running",
    docker_type VARCHAR(10) DEFAULT "simple",
    submission_time TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);





-- Linking table to associate users with groups
CREATE TABLE group_users (
    group_id INT REFERENCES groups(group_id),
    user_id INT REFERENCES users(user_id),
    PRIMARY KEY (group_id, user_id)
);
