package com.ugent.pidgeon.controllers;
import com.ugent.pidgeon.auth.Roles;
import com.ugent.pidgeon.model.Auth;
import com.ugent.pidgeon.model.User;
import com.ugent.pidgeon.postgre.models.types.UserRole;
import com.ugent.pidgeon.postgre.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class AuthTestController {
    @Autowired
    private UserRepository userRepository;


    @GetMapping("/api/test")
    @Roles({ UserRole.teacher})
    public User testApi(HttpServletRequest request, Auth auth) {
        return auth.getUser();
    }

    @DeleteMapping("/api/test")
    public Object postTest(@RequestBody Object requestBody){
        return requestBody;
    }

    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        return ResponseEntity.status(HttpStatus.OK).body("Pong");
    }

    @GetMapping("/")
    public ResponseEntity<String> index() {
        return ResponseEntity.status(HttpStatus.OK).body("Running...");
    }

}
