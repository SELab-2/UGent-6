package com.ugent.pidgeon.util;

import com.ugent.pidgeon.controllers.CheckResult;
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

    public CheckResult checkUserUpdateJson(UserUpdateJson json) {
        if (json.getName() == null || json.getSurname() == null || json.getEmail() == null || json.getRole() == null) {
            return new CheckResult(HttpStatus.BAD_REQUEST, "name, surname, email and role are required");
        }

        if (json.getRoleAsEnum() == null) {
            return new CheckResult(HttpStatus.BAD_REQUEST, "Role is not valid: must be either student, admin or teacher");
        }

        if (json.getName().isBlank()) {
            return new CheckResult(HttpStatus.BAD_REQUEST, "Name cannot be empty");
        }

        if (json.getSurname().isBlank()) {
            return new CheckResult(HttpStatus.BAD_REQUEST, "Surname cannot be empty");
        }

        if (!StringMatcher.isValidEmail(json.getEmail())) {
            return new CheckResult(HttpStatus.BAD_REQUEST, "Email is not valid");
        }

        return new CheckResult(HttpStatus.OK, "");
    }

}
