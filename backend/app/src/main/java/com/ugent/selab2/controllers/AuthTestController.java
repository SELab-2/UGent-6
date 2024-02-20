package com.ugent.selab2.controllers;
import com.ugent.selab2.config.JwtAuth;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthTestController {

    /*@GetMapping("Admin")
    @ResponseBody
    @PreAuthorize("hasAuthority('APPROLE_Admin')")
    public String Admin() {
        return "Admin message";
    }*/
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/api/test")
    public String testApi(HttpServletRequest request) {
        return "Access granted for authenticated user!";
       /* String token = getTokenFromRequest(request);
        if (token != null) {
            JwtAuth jwtAuth = new JwtAuth();
            jwtAuth.verify(token);
            System.out.println("Token: " + token);
        } else {
            return "No token found!";
        }

        // Access user information using SecurityContextHolder if needed
        return "Access granted for authenticated user!"; */
    }

    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
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
