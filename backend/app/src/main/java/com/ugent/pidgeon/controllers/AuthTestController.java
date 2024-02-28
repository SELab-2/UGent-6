package com.ugent.selab2.controllers;
import com.ugent.selab2.model.Auth;
import com.ugent.selab2.model.User;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthTestController {

    @GetMapping("/api/test")
    public User testApi(HttpServletRequest request, Auth auth) {
        return auth.getUser();
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
