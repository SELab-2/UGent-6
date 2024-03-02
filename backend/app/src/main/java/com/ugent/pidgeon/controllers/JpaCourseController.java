package com.ugent.pidgeon.controllers;

import com.ugent.pidgeon.postgre.models.CourseEntity;
import com.ugent.pidgeon.postgre.models.UserEntity;
import com.ugent.pidgeon.postgre.repository.CourseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class JpaCourseController {
    @Autowired
    private CourseRepository courseRepository;

    @GetMapping("/api/courses")
    public String getCourses() {
        StringBuilder res = new StringBuilder();
        for (CourseEntity course : courseRepository.findAll()) {
            res.append(course.getName()).append(" with users: ");
            for (CourseRepository.UserWithRelation user : courseRepository.findUsersByCourseId(course.getId())) {
                UserEntity userEntity = user.getUser();
                String relation = user.getRelation();
                res.append(userEntity.getName()).append("(").append(relation).append("), ");
            }
            res.append("\n");
        }

        return res.toString();
    }

    @GetMapping("/api/course")
    public String addCourse(String name, String description) {
        CourseEntity course = new CourseEntity();
        course.setId(1);
        course.setName("Test");
        course.setDescription("Added for testing update purposes");
        courseRepository.save(course);
        return "Course added";
    }
}