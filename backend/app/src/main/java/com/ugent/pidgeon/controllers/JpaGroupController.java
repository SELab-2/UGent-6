package com.ugent.pidgeon.controllers;

import com.ugent.pidgeon.postgre.models.GroupEntity;
import com.ugent.pidgeon.postgre.models.UserEntity;
import com.ugent.pidgeon.postgre.repository.GroupRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
public class JpaGroupController {
    @Autowired
    private GroupRepository groupRepository;

    @GetMapping("/api/groups")
    public List<String> getGroups() {
        List<String> res = new ArrayList<>();
        for (GroupEntity group : groupRepository.findAll()) {
            StringBuilder groupString = new StringBuilder();
            groupString.append(group.getName()).append(" with users: ");
            for (UserEntity user : groupRepository.findCourseUsersByGroupId(group.getId())) {
                groupString.append(user.getName()).append(", ");
            }
            res.add(groupString.toString());
        }
        return res;
    }
}
