package com.ugent.pidgeon.controllers;


import com.ugent.pidgeon.postgre.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class JpaUserController {
    /*@Autowired
    private UserRepository userRepository;*/

    @GetMapping("/api/users")
    public String getUsers() {
        return "kaas+: ";
    }
}
