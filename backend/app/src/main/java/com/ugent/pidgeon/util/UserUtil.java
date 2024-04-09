package com.ugent.pidgeon.util;

import com.ugent.pidgeon.model.json.UserReferenceJson;
import com.ugent.pidgeon.model.json.UserUpdateJson;
import com.ugent.pidgeon.postgre.models.UserEntity;
import com.ugent.pidgeon.postgre.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class UserUtil {

    @Autowired
    private UserRepository userRepository;

    public boolean userExists(long userId) {
        return userRepository.existsById(userId);
    }

    public UserEntity getUserIfExists(long userId) {
        return userRepository.findById(userId).orElse(null);
    }

    public CheckResult<UserEntity> checkForUserUpdateJson(long userId, UserUpdateJson json) {
        UserEntity user = getUserIfExists(userId);
        if (user == null) {
            return new CheckResult<>(HttpStatus.NOT_FOUND, "User not found", null);
        }
        if (json.getName() == null || json.getSurname() == null || json.getEmail() == null || json.getRole() == null) {
            return new CheckResult<>(HttpStatus.BAD_REQUEST, "name, surname, email and role are required", null);
        }

        if (json.getRoleAsEnum() == null) {
            return new CheckResult<>(HttpStatus.BAD_REQUEST, "Role is not valid: must be either student, admin or teacher", null);
        }

        if (json.getName().isBlank()) {
            return new CheckResult<>(HttpStatus.BAD_REQUEST, "Name cannot be empty", null);
        }

        if (json.getSurname().isBlank()) {
            return new CheckResult<>(HttpStatus.BAD_REQUEST, "Surname cannot be empty", null);
        }

        if (!StringMatcher.isValidEmail(json.getEmail())) {
            return new CheckResult<>(HttpStatus.BAD_REQUEST, "Email is not valid", null);
        }

        return new CheckResult<>(HttpStatus.OK, "", user);
    }



}
