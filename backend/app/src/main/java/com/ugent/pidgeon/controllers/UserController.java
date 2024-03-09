package com.ugent.pidgeon.controllers;

import com.ugent.pidgeon.auth.Roles;
import com.ugent.pidgeon.model.Auth;
import com.ugent.pidgeon.model.json.CourseWithRelationJson;
import com.ugent.pidgeon.model.json.UserJson;
import com.ugent.pidgeon.postgre.models.types.UserRole;
import com.ugent.pidgeon.postgre.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
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
    public UserJson getUserById(@PathVariable("userid") Long userid) {
        UserJson res = userRepository.findById(userid).map(UserJson::new).orElse(null);
        if (res == null) {
            return null;
        }
        List<UserRepository.CourseIdWithRelation> courses = userRepository.findCourseIdsByUserId(userid);

        res.setCourses(courses.stream().map(
                c -> new CourseWithRelationJson(
                        ApiRoutes.COURSE_BASE_PATH + c.getCourseId(),
                            c.getRelation()
                )).toList());
        return res;
    }


}
