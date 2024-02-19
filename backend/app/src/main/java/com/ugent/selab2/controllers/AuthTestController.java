package com.ugent.selab2.controllers;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthTestController {

    /*@GetMapping("Admin")
    @ResponseBody
    @PreAuthorize("hasAuthority('APPROLE_Admin')")
    public String Admin() {
        return "Admin message";
    }*/


    @GetMapping("/ping")
    public String ping() {
        return "Pong";
    }

    @GetMapping("/")
    public String index() {
        return "Running...";
    }

}
