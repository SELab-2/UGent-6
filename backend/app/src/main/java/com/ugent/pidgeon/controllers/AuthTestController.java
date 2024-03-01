package com.ugent.pidgeon.controllers;
import com.ugent.pidgeon.model.Auth;
import com.ugent.pidgeon.model.User;
import com.ugent.pidgeon.postgre.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthTestController {
    @Autowired
    private UserRepository userRepository;

    @GetMapping("/api/test")
    public User testApi(HttpServletRequest request, Auth auth) {
        return auth.getUser();
    }

    @GetMapping("/api/users")
    public String getUsers() {
        return "kaas+: " /*+ userRepository.findAll().get(0).getName()*/;
    }

    @GetMapping("/ping")
    public String ping() {
        return "Pong";
    }

    @GetMapping("/")
    public String index() {
        return "Running!!!...";
    }

}
