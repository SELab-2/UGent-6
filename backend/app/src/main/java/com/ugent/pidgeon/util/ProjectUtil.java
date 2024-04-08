package com.ugent.pidgeon.util;

import com.ugent.pidgeon.controllers.CheckResult;
import com.ugent.pidgeon.postgre.models.ProjectEntity;
import com.ugent.pidgeon.postgre.models.UserEntity;
import com.ugent.pidgeon.postgre.models.types.UserRole;
import com.ugent.pidgeon.postgre.repository.ProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class ProjectUtil {
    @Autowired
    private ProjectRepository projectRepository;

    public ProjectEntity getProjectIfExists(long projectId) {
        return projectRepository.findById(projectId).orElse(null);
    }

    public CheckResult isProjectAdmin(long projectId, UserEntity user) {
        if(!projectRepository.adminOfProject(projectId, user.getId()) && !user.getRole().equals(UserRole.admin)) {
            return new CheckResult(HttpStatus.FORBIDDEN, "You are not and admin of this project");
        }
        return new CheckResult(HttpStatus.OK, "");
    }

}
