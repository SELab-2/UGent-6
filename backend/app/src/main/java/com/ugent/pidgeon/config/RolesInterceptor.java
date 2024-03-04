package com.ugent.pidgeon.config;


import com.ugent.pidgeon.postgre.models.types.UserRole;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

@Component
public class RolesInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            Roles rolesAnnotation = handlerMethod.getMethodAnnotation(Roles.class);
            if (rolesAnnotation != null) {
                List<UserRole> requiredRoles = Arrays.asList(rolesAnnotation.value());
                // Implement your own logic to check if the user has the required role
                if (!hasRequiredRole(request, requiredRoles)) {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    return false;
                }
            }
        }
        return true;
    }

    private boolean hasRequiredRole(HttpServletRequest request, List<UserRole> requiredRoles) {
        // Example: Check if the user's role matches any of the required roles
        // You can access the user's role from the request (e.g., from a session or token)
        // Return true if the user has the required role, otherwise return false
        return true;
    }


}
