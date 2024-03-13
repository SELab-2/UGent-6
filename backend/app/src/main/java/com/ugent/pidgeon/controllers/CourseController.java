package com.ugent.pidgeon.controllers;

import com.ugent.pidgeon.auth.Roles;
import com.ugent.pidgeon.postgre.models.CourseEntity;
import com.ugent.pidgeon.postgre.models.CourseUserEntity;
import com.ugent.pidgeon.postgre.models.CourseUserId;
import com.ugent.pidgeon.postgre.models.ProjectEntity;
import com.ugent.pidgeon.postgre.models.types.CourseRelation;
import com.ugent.pidgeon.postgre.models.types.UserRole;
import com.ugent.pidgeon.postgre.repository.CourseRepository;
import com.ugent.pidgeon.postgre.repository.CourseUserRepository;
import com.ugent.pidgeon.postgre.repository.ProjectRepository;
import com.ugent.pidgeon.postgre.repository.TestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
public class CourseController {

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private TestRepository testRepository;

    @Autowired
    private CourseUserRepository courseUserRepository;

    @GetMapping(ApiRoutes.COURSE_BASE_PATH + "/{courseId}")
    @Roles({UserRole.teacher, UserRole.student})
    public ResponseEntity<CourseEntity> getCourseByCourseId(@PathVariable Long courseId) {
        Optional<CourseEntity> courseopt = courseRepository.findById(courseId);
        if (courseopt.isEmpty()) {
            return ResponseEntity.notFound().build(); // Or return an empty list, based on your preference
        }
        CourseEntity course = courseopt.get();
        return ResponseEntity.ok(course);
    }

    @GetMapping(ApiRoutes.COURSE_BASE_PATH + "/{courseId}/projects")
    @Roles({UserRole.teacher, UserRole.student})
    public ResponseEntity<List<ProjectEntity>> getProjectByCourseId(@PathVariable Long courseId) {
        List<ProjectEntity> projects = projectRepository.findByCourseId(courseId);
        if (projects.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(projects);
    }

    @DeleteMapping(ApiRoutes.COURSE_BASE_PATH + "/{courseId}/members/")
    @Roles({UserRole.teacher, UserRole.student})
    public ResponseEntity<?> removeCourseMember(@PathVariable Long courseId, @RequestParam Long studentId) {
        courseUserRepository.deleteById(new CourseUserId(courseId, studentId));
        return ResponseEntity.ok().build(); // Successfully removed
    }

    @PostMapping(ApiRoutes.COURSE_BASE_PATH + "/{courseId}/members")
    @Roles({UserRole.teacher, UserRole.student})
    public ResponseEntity<?> addCourseMember(@PathVariable Long courseId, @RequestParam Long userId, @RequestParam CourseRelation relation) {
        courseUserRepository.save(new CourseUserEntity(courseId, userId, relation));
        return ResponseEntity.status(HttpStatus.CREATED).build(); // Successfully added
    }

    @PatchMapping(ApiRoutes.COURSE_BASE_PATH + "/{courseId}/members")
    @Roles({UserRole.teacher, UserRole.student})
    public ResponseEntity<?> updateCourseMember(@PathVariable Long courseId, @RequestParam Long userId, @RequestParam CourseRelation relation) {
        Optional<CourseUserEntity> ce = courseUserRepository.findById(new CourseUserId(courseId, userId));
        if(ce.isPresent()){
            ce.get().setRelation(relation);
            courseUserRepository.save(ce.get());
            return ResponseEntity.ok().build();
        }else{
            return ResponseEntity.notFound().build(); // Relation not found
        }
    }

    @GetMapping(ApiRoutes.COURSE_BASE_PATH + "/{courseId}/members")
    @Roles({UserRole.teacher, UserRole.student})
    public ResponseEntity<List<CourseUserEntity>> getCourseMembers(@PathVariable Long courseId) {
        List<CourseUserEntity> members = courseUserRepository.findAllMembers(courseId);
        return ResponseEntity.ok(members); // Successfully retrieved members
    }
}
