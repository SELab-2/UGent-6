package com.ugent.pidgeon.controllers;
import com.ugent.pidgeon.auth.Roles;
import com.ugent.pidgeon.model.Auth;
import com.ugent.pidgeon.model.User;
import com.ugent.pidgeon.postgre.models.types.UserRole;
import com.ugent.pidgeon.postgre.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthTestController {
    @Autowired
    private UserRepository userRepository;


    @GetMapping("/api/test")
    @Roles({UserRole.student, UserRole.teacher})
    public User testApi(HttpServletRequest request, Auth auth) {
        return auth.getUser();
    }

    @PostMapping("/api/test")
    public Object postTest(@RequestBody Object requestBody){
        return requestBody;
    }

    @GetMapping("/ping")
    public String ping() {
        return "Pong";
    }

    @GetMapping("/")
    public String index() {
        return "Running...";
    }

}
