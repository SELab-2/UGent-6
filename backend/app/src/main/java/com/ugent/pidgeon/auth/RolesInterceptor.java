package com.ugent.pidgeon.auth;


import com.ugent.pidgeon.model.Auth;
import com.ugent.pidgeon.postgre.models.UserEntity;
import com.ugent.pidgeon.postgre.models.types.UserRole;
import com.ugent.pidgeon.postgre.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;
import java.util.List;

@Component
public class RolesInterceptor implements HandlerInterceptor {


    private final UserRepository userRepository;

    @Autowired
    public RolesInterceptor(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (handler instanceof HandlerMethod handlerMethod) {
            Roles rolesAnnotation = handlerMethod.getMethodAnnotation(Roles.class);
            if (rolesAnnotation != null) {
                List<UserRole> requiredRoles = Arrays.asList(rolesAnnotation.value());
                Auth auth = (Auth) SecurityContextHolder.getContext().getAuthentication();
                UserEntity userEntity = userRepository.findUserByAzureId(auth.getOid());

                if(userEntity == null) {
                    System.out.println("User does not exist, creating new one");
                    userEntity = new UserEntity(auth.getUser().firstName,auth.getUser().lastName, auth.getEmail(), UserRole.student, auth.getOid());
                    userRepository.save(userEntity);
                }
                auth.setUserEntity(userEntity);

                if (!requiredRoles.contains(userEntity.getRole()) || userEntity.getRole() == UserRole.admin) {
                    response.sendError(HttpServletResponse.SC_FORBIDDEN, "User does not have required role");
                    return false;
                }
            }
        }
        return true;
    }
}
