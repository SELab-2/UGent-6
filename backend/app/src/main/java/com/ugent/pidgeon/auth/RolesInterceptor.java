package com.ugent.pidgeon.auth;


import com.ugent.pidgeon.model.Auth;
import com.ugent.pidgeon.postgre.models.UserEntity;
import com.ugent.pidgeon.postgre.models.types.UserRole;
import com.ugent.pidgeon.postgre.repository.UserRepository;
import com.ugent.pidgeon.util.DataGeneration;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;


/**
 * This class is a Spring component that implements the HandlerInterceptor interface.
 * It is used to intercept HTTP requests and perform role-based access control.
 */
@Component
public class RolesInterceptor implements HandlerInterceptor {

    // UserRepository instance for interacting with the user data in the database
    private final UserRepository userRepository;

    @Autowired
    private DataGeneration dg;

    /**
     * Constructor for RolesInterceptor.
     * @param userRepository UserRepository instance for interacting with the user data in the database
     */
    @Autowired
    public RolesInterceptor(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * This method is called before the actual handler is executed.
     * It checks if the handler is a HandlerMethod and if it has a Roles annotation.
     * If the Roles annotation is present, it checks if the authenticated user has the required role.
     * If the user does not exist, it creates a new user with the role of 'student'.
     * If the user does not have the required role, it sends an HTTP 403 error and returns false.
     * @param request HttpServletRequest that is being processed
     * @param response HttpServletResponse that is being created
     * @param handler chosen handler to execute, for type and/or instance evaluation
     * @return true if the execution chain should proceed with the next interceptor or the handler itself
     * @throws Exception in case of errors
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (handler instanceof HandlerMethod handlerMethod) {
            Roles rolesAnnotation = handlerMethod.getMethodAnnotation(Roles.class);
            if (rolesAnnotation != null) {
                List<UserRole> requiredRoles = Arrays.asList(rolesAnnotation.value());
                Auth auth = (Auth) SecurityContextHolder.getContext().getAuthentication();
                UserEntity userEntity = userRepository.findUserByAzureId(auth.getOid()).orElse(null);

                if(userEntity == null) {
                    System.out.println("User does not exist, creating new one. user_id: " + auth.getOid());
                    userEntity = new UserEntity(auth.getUser().firstName,auth.getUser().lastName, auth.getEmail(), UserRole.admin, auth.getOid(), auth.getStudentNumber());
                    OffsetDateTime now = OffsetDateTime.now();
                    userEntity.setCreatedAt(now);
                    userEntity = userRepository.save(userEntity);
                    System.out.println("User created with id: " + userEntity.getId());


                    dg.generate(auth);
                }
                auth.setUserEntity(userEntity);

                if (!requiredRoles.contains(userEntity.getRole()) && userEntity.getRole() != UserRole.admin) {
                    response.sendError(HttpServletResponse.SC_FORBIDDEN, "User does not have required role");
                    return false;
                }
            }
        }
        return true;
    }
}
