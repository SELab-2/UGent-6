package com.ugent.pidgeon.controllers;

import com.ugent.pidgeon.auth.Roles;
import com.ugent.pidgeon.model.Auth;
import com.ugent.pidgeon.model.json.ProjectUpdateDTO;
import com.ugent.pidgeon.postgre.models.DeadlineEntity;
import com.ugent.pidgeon.postgre.models.ProjectEntity;
import com.ugent.pidgeon.postgre.models.types.UserRole;
import com.ugent.pidgeon.postgre.repository.DeadlineRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
public class DeadlineController {

    @Autowired
    private DeadlineRepository deadlineRepository;

    @DeleteMapping(ApiRoutes.DEADLINE_BASE_PATH  + "/{deadlineId}")
    @Roles({UserRole.admin, UserRole.teacher})
    public ResponseEntity<?> deleteDeadlineById(@PathVariable Long deadlineId, Auth auth){
        //TODO:in future versions check if user is allowed to delete deadline
        Optional<DeadlineEntity> deadlineOptional = deadlineRepository.findById(deadlineId);
        if(deadlineOptional.isPresent()){
            deadlineRepository.delete(deadlineOptional.get());
            return ResponseEntity.ok(deadlineOptional);

        }else{
            return ResponseEntity.notFound().build();
        }
    }


}
