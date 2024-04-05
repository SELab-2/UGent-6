package com.ugent.pidgeon.controllers;

import com.ugent.pidgeon.auth.Roles;
import com.ugent.pidgeon.model.Auth;
import com.ugent.pidgeon.model.json.CourseWithRelationJson;
import com.ugent.pidgeon.model.json.UserJson;
import com.ugent.pidgeon.postgre.models.UserEntity;
import com.ugent.pidgeon.postgre.models.types.UserRole;
import com.ugent.pidgeon.postgre.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class UserController {

    @Autowired
    private UserRepository userRepository;

    /**
     * Function to get a user by id
     *
     * @param userid identifier of a user
     * @param auth   authentication object
     * @HttpMethod GET
     * @ApiPath /api/user/{userid}
     * @AllowedRoles student
     * @ApiDog <a href="https://apidog.com/apidoc/project-467959/api-5723824">apiDog documentation</a>
     * @return user object
     */
    @GetMapping(ApiRoutes.USER_BASE_PATH + "/{userid}")
    @Roles({UserRole.student})
    public ResponseEntity<Object> getUserById(@PathVariable("userid") Long userid,Auth auth) {
        UserEntity user = auth.getUserEntity();
        if (user.getId() != userid) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You do not have access to this user");
        }

        UserJson res = userRepository.findById(userid).map(UserJson::new).orElse(null);
        if (res == null) {
            return  ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        return ResponseEntity.ok().body(res);
    }



    
    @GetMapping(ApiRoutes.USER_AUTH_PATH)
    @Roles({UserRole.student, UserRole.teacher})
    public ResponseEntity<Object> getUserByAzureId(Auth auth) {
        UserEntity res = auth.getUserEntity();
        UserJson userJson = new UserJson(res);
        return ResponseEntity.ok().body(userJson);
    }


}
