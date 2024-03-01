package com.ugent.pidgeon.controllers;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;
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
        logger.info("test");
        userRepository.findAll().forEach(user -> logger.info(user.getName()));
        return "kaas+: " + userRepository.findById(2).get(0).getName();
    }
}
