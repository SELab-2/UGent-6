package com.ugent.pidgeon.controllers;
import com.ugent.pidgeon.model.Auth;
import com.ugent.pidgeon.model.User;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthTestController {

    @GetMapping("/api/test")
    public User testApi(HttpServletRequest request, Auth auth) {
        return auth.getUser();
    }

    @PostMapping("/api/test2")
    public String postTest(){
        return "Post test succeeded!";
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
