package com.ugent.pidgeon.controllers;

import com.ugent.pidgeon.auth.Roles;
import com.ugent.pidgeon.json.UserJson;
import com.ugent.pidgeon.json.UserUpdateJson;
import com.ugent.pidgeon.model.Auth;
import com.ugent.pidgeon.postgre.models.UserEntity;
import com.ugent.pidgeon.postgre.models.types.UserRole;
import com.ugent.pidgeon.postgre.repository.UserRepository;
import com.ugent.pidgeon.util.CheckResult;
import com.ugent.pidgeon.util.UserUtil;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
    @GetMapping(ApiRoutes.USERS_BASE_PATH + "/{userid}")
    @Roles({UserRole.student, UserRole.teacher})
    public ResponseEntity<Object> getUserById(@PathVariable("userid") Long userid,Auth auth) {
        UserEntity requester = auth.getUserEntity();
        if (requester.getId() != userid && requester.getRole() != UserRole.admin) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You do not have access to this user");
        }

        UserEntity user = userUtil.getUserIfExists(userid);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        UserJson res = new UserJson(user);

        return ResponseEntity.ok().body(res);
    }

    /**
     * Function to search users by email, name and surname
     *
     * @param email   email of a user
     * @param name    name of a user
     * @param surname surname of a user
     * @HttpMethod GET
     * @ApiPath /api/user
     * @AllowedRoles admin
     * @ApiDog <a href="https://apidog.com/apidoc/project-467959/api-7405120">apiDog documentation</a>
     * @return user object
     */
    @GetMapping(ApiRoutes.USERS_BASE_PATH)
    @Roles({UserRole.admin})
    public ResponseEntity<Object> getUsersByNameOrSurname(
        @RequestParam(value="email", required = false) String email,
        @RequestParam(value = "name", required = false) String name,
        @RequestParam(value = "surname", required = false) String surname
    ) {
        if (email != null) {

            UserEntity user = userRepository.findByEmail(email);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.OK).body(new ArrayList<>());
            }
            if (name != null && !user.getName().toLowerCase().contains(name.toLowerCase())) {
                return ResponseEntity.ok().body(new ArrayList<>());
            } else if (surname != null && !user.getSurname().toLowerCase().contains(surname.toLowerCase())) {
                return ResponseEntity.ok().body(new ArrayList<>());
            }

            return ResponseEntity.ok().body(List.of(new UserJson(user)));
        }

        if ((name == null || name.length() < 3) && (surname == null || surname.length() < 3)) {
            return ResponseEntity.status(HttpStatus.OK).body(new ArrayList<>());
        }

        if (name == null) name = "";
        if (surname == null) surname = "";

        List<UserEntity> usersByName = userRepository.findByName(name, surname);


        return ResponseEntity.ok().body(usersByName.stream().map(UserJson::new).toList());
    }

    /**
     * Function to get the logged in user
     *
     * @param auth authentication object
     * @HttpMethod GET
     * @ApiPath /api/user
     * @AllowedRoles student
     * @ApiDog <a href="https://apidog.com/apidoc/project-467959/api-7405497">apiDog documentation</a>
     * @return user object
     */
    @GetMapping(ApiRoutes.LOGGEDIN_USER_PATH)
    @Roles({UserRole.student, UserRole.teacher})
    public ResponseEntity<Object> getLoggedInUser(Auth auth) {
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

    /**
     * Function to edit a user by id
     *
     * @param userid identifier of a user
     * @param auth   authentication object
     * @HttpMethod PUT
     * @ApiPath /api/user/{userid}
     * @AllowedRoles admin
     * @ApiDog <a href="https://apidog.com/apidoc/project-467959/api-6693479">apiDog documentation</a>
     * @return string
     */
    @PutMapping(ApiRoutes.USERS_BASE_PATH + "/{userid}")
    @Roles({UserRole.admin})
    public ResponseEntity<?> updateUserById(@PathVariable("userid") Long userid, @RequestBody UserUpdateJson userUpdateJson, Auth auth) {

        CheckResult<UserEntity> checkResult = userUtil.checkForUserUpdateJson(userid, userUpdateJson);
        if (checkResult.getStatus() != HttpStatus.OK) {
            return ResponseEntity.status(checkResult.getStatus()).body(checkResult.getMessage());
        }

        return doUserUpdate(checkResult.getData(), userUpdateJson);
    }

    /**
     * Function to edit a user by id
     *
     * @param userid identifier of a user
     * @param auth   authentication object
     * @HttpMethod PATCH
     * @ApiPath /api/user/{userid}
     * @AllowedRoles admin
     * @ApiDog <a href="https://apidog.com/apidoc/project-467959/api-6693481">apiDog documentation</a>
     * @return string
     */
    @PatchMapping(ApiRoutes.USERS_BASE_PATH + "/{userid}")
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

        if (userUpdateJson.getRole() == null) {
            userUpdateJson.setRole(user.getRole().toString());
        }

        CheckResult<UserEntity> checkResult = userUtil.checkForUserUpdateJson(userid, userUpdateJson);
        if (checkResult.getStatus() != HttpStatus.OK) {
            return ResponseEntity.status(checkResult.getStatus()).body(checkResult.getMessage());
        }

        return doUserUpdate(user, userUpdateJson);
    }

}
