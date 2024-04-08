package com.ugent.pidgeon.controllers;

import com.ugent.pidgeon.auth.Roles;
import com.ugent.pidgeon.model.Auth;
import com.ugent.pidgeon.model.json.UserJson;
import com.ugent.pidgeon.model.json.UserUpdateJson;
import com.ugent.pidgeon.postgre.models.UserEntity;
import com.ugent.pidgeon.postgre.models.types.UserRole;
import com.ugent.pidgeon.postgre.repository.UserRepository;
import com.ugent.pidgeon.util.UserUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.List;

@RestController
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserUtil userUtil;

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
        UserEntity requester = auth.getUserEntity();
        if (requester.getId() != userid) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You do not have access to this user");
        }

        UserEntity user = userUtil.getUserIfExists(userid);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        UserJson res = new UserJson(user);

        return ResponseEntity.ok().body(res);
    }



    
    @GetMapping(ApiRoutes.USER_AUTH_PATH)
    @Roles({UserRole.student, UserRole.teacher})
    public ResponseEntity<Object> getUserByAzureId(Auth auth) {
        UserEntity res = auth.getUserEntity();
        UserJson userJson = new UserJson(res);
        return ResponseEntity.ok().body(userJson);
    }


    private ResponseEntity<?> doUserUpdate(UserEntity user, UserUpdateJson json) {
        user.setName(json.getName());
        user.setSurname(json.getSurname());
        user.setEmail(json.getEmail());
        user.setRole(json.getRoleAsEnum());
        userRepository.save(user);
        return ResponseEntity.ok().body(new UserJson(user));
    }

    @PutMapping(ApiRoutes.USER_BASE_PATH + "/{userid}")
    @Roles({UserRole.admin})
    public ResponseEntity<?> updateUserById(@PathVariable("userid") Long userid, @RequestBody UserUpdateJson userUpdateJson, Auth auth) {
        UserEntity user = userUtil.getUserIfExists(userid);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        CheckResult checkResult = userUtil.checkUserUpdateJson(userUpdateJson);
        if (checkResult.getStatus() != HttpStatus.OK) {
            return ResponseEntity.status(checkResult.getStatus()).body(checkResult.getMessage());
        }

        return doUserUpdate(user, userUpdateJson);
    }

    @PatchMapping(ApiRoutes.USER_BASE_PATH + "/{userid}")
    @Roles({UserRole.admin})
    public ResponseEntity<?> patchUserById(@PathVariable("userid") Long userid, @RequestBody UserUpdateJson userUpdateJson, Auth auth) {
        UserEntity user = userUtil.getUserIfExists(userid);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        if (userUpdateJson.getName() == null) {
            userUpdateJson.setName(user.getName());
        }
        if (userUpdateJson.getSurname() == null) {
            userUpdateJson.setSurname(user.getSurname());
        }
        if (userUpdateJson.getEmail() == null) {
            userUpdateJson.setEmail(user.getEmail());
        }

        Logger.getGlobal().info(userUpdateJson.getRole());
        if (userUpdateJson.getRole() == null) {
            userUpdateJson.setRole(user.getRole().toString());
        }

        CheckResult checkResult = userUtil.checkUserUpdateJson(userUpdateJson);
        if (checkResult.getStatus() != HttpStatus.OK) {
            return ResponseEntity.status(checkResult.getStatus()).body(checkResult.getMessage());
        }

        return doUserUpdate(user, userUpdateJson);
    }

}
