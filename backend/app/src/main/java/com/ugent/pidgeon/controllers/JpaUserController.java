    package com.ugent.pidgeon.controllers;

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
        @GetMapping("/api/users2")
        public String getUsers() {
            UserEntity user = userRepository.findById(2).get(0);
            userRepository.findCoursesByUserId(user.getId()).forEach(course -> logger.info(course.getName()));
            return "kaas+: " + user.getName();
        }
    }
