package com.ugent.pidgeon.util;

import com.ugent.pidgeon.controllers.CourseController;
import com.ugent.pidgeon.model.Auth;
import com.ugent.pidgeon.postgre.models.UserEntity;
import com.ugent.pidgeon.postgre.models.types.UserRole;
import com.ugent.pidgeon.postgre.repository.CourseRepository;
import com.ugent.pidgeon.postgre.repository.CourseUserRepository;
import com.ugent.pidgeon.postgre.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class DataGeneration {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CourseController courseController;

    

    public void generate(Auth auth) {
        makeFakeUsers();
    }

    private void makeFakeUsers() {
        for (int i = 0; i < 50; i++) {
            UserEntity user = new UserEntity(
                    "student",
                    "number ".concat(String.valueOf(i)),
                    "student.number".concat(String.valueOf(i)).concat("@ugent.be"),
                    UserRole.student,
                    "azure_id_number_".concat(String.valueOf(i)),
                    String.valueOf(i * 1000)
            );
            userRepository.save(user);

        }
    }

}
