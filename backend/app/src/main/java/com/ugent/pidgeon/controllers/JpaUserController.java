    package com.ugent.pidgeon.controllers;

    import com.ugent.pidgeon.postgre.models.CourseEntity;
    import com.ugent.pidgeon.postgre.models.UserEntity;
    import com.ugent.pidgeon.postgre.repository.UserRepository;
    import org.slf4j.Logger;
    import org.slf4j.LoggerFactory;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.web.bind.annotation.GetMapping;
    import org.springframework.web.bind.annotation.RestController;

    @RestController
    public class JpaUserController {
        @Autowired
        private UserRepository userRepository;

        Logger logger = LoggerFactory.getLogger(JpaUserController.class);
        @GetMapping("/api/users")
        public String getUsers() {
            StringBuilder res = new StringBuilder();
            for (UserEntity user : userRepository.findAll()) {
                res.append(user.getName()).append("(").append(user.getRole().toString()).append(") in courses: ");
                for (CourseEntity course : userRepository.findCoursesByUserId(user.getId())) {
                    res.append(course.getName()).append(", ");
                }
                res.append("\n");
            }

            return res.toString();
        }
    }
