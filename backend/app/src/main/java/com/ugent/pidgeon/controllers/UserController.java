package com.ugent.pidgeon.controllers;

import com.ugent.pidgeon.auth.Roles;
import com.ugent.pidgeon.model.Auth;
import com.ugent.pidgeon.model.json.CourseWithRelationJson;
import com.ugent.pidgeon.model.json.UserJson;
import com.ugent.pidgeon.postgre.models.UserEntity;
import com.ugent.pidgeon.postgre.models.types.UserRole;
import com.ugent.pidgeon.postgre.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping(ApiRoutes.USER_BASE_PATH + "/{userid}")
    @Roles({UserRole.student, UserRole.teacher})
    public ResponseEntity<Object> getUserById(@PathVariable("userid") Long userid) {
        UserJson res = userRepository.findById(userid).map(UserJson::new).orElse(null);
        if (res == null) {
            return  ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        return ResponseEntity.ok().body(res);
    }


    @GetMapping(ApiRoutes.USER_COURSES_BASE_PATH)
    @Roles({UserRole.student, UserRole.teacher})
    public ResponseEntity<Object> getUserCourses(@PathVariable("userid") Long userid,Auth auth) {
        UserEntity user = auth.getUserEntity();
        if (userid != user.getId() && user.getRole() != UserRole.teacher) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("User does not have access to this user's courses");
        }

        List<UserRepository.CourseIdWithRelation> courses = userRepository.findCourseIdsByUserId(userid);

        List<CourseWithRelationJson> userCourses = courses.stream().map(
                c -> new CourseWithRelationJson(
                        ApiRoutes.COURSE_BASE_PATH + c.getCourseId(),
                            c.getRelation(),
                            c.getName()

                )).toList();

        return ResponseEntity.ok().body(userCourses);
    }

}
