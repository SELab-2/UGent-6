package com.ugent.pidgeon.controllers;

import com.ugent.pidgeon.auth.Roles;
import com.ugent.pidgeon.model.Auth;
import com.ugent.pidgeon.model.json.GroupJson;
import com.ugent.pidgeon.model.json.NameRequest;
import com.ugent.pidgeon.postgre.models.GroupEntity;
import com.ugent.pidgeon.postgre.models.UserEntity;
import com.ugent.pidgeon.postgre.models.types.UserRole;
import com.ugent.pidgeon.postgre.repository.GroupRepository;
import com.ugent.pidgeon.util.CheckResult;
import com.ugent.pidgeon.util.CommonDatabaseActions;
import com.ugent.pidgeon.util.EntityToJsonConverter;
import com.ugent.pidgeon.util.GroupUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
public class GroupController {
    @Autowired
    private GroupRepository groupRepository;
    @Autowired
    private GroupUtil groupUtil;
    @Autowired
    private EntityToJsonConverter entityToJsonConverter;
    @Autowired
    private CommonDatabaseActions commonDatabaseActions;


    /**
     * Function to add a new project to an existing course
     *
     * @param groupid identifier of a group
     * @param auth    authentication object of the requesting user
     * @return ResponseEntity<GroupJson>
     * @ApiDog <a href="https://apidog.com/apidoc/project-467959/api-5723981">apiDog documentation</a>
     * @HttpMethod Get
     * @AllowedRoles student, teacher
     * @ApiPath /api/groups/{groupid}
     */
    @GetMapping(ApiRoutes.GROUP_BASE_PATH + "/{groupid}")
    @Roles({UserRole.student, UserRole.teacher})
    public ResponseEntity<?> getGroupById(@PathVariable("groupid") Long groupid, Auth auth) {

        CheckResult<GroupEntity> checkResult = groupUtil.getGroupIfExists(groupid);
        if (checkResult.getStatus() != HttpStatus.OK) {
            return ResponseEntity.status(checkResult.getStatus()).body(checkResult.getMessage());
        }
        GroupEntity group = checkResult.getData();
        CheckResult<Void> checkResult1 = groupUtil.canGetGroup(groupid, auth.getUserEntity());
        if (checkResult1.getStatus() != HttpStatus.OK) {
            return ResponseEntity.status(checkResult1.getStatus()).body(checkResult1.getMessage());
        }

        // Return the group
        GroupJson groupJson = entityToJsonConverter.groupEntityToJson(group);
        return ResponseEntity.ok(groupJson);
    }

    /**
     * Function to update the name of a group
     *
     * @param groupid     identifier of a group
     * @param nameRequest object containing the new name of the group
     * @param auth        authentication object of the requesting user
     * @return ResponseEntity<GroupJson>
     * @ApiDog <a href="https://apidog.com/apidoc/project-467959/api-5723995">apiDog documentation</a>
     * @HttpMethod Put
     * @AllowedRoles teacher
     * @ApiPath /api/groups/{groupid}
     */
    @PutMapping(ApiRoutes.GROUP_BASE_PATH + "/{groupid}")
    @Roles({UserRole.teacher})
    public ResponseEntity<?> updateGroupName(@PathVariable("groupid") Long groupid, @RequestBody NameRequest nameRequest, Auth auth) {
        return doGroupNameUpdate(groupid, nameRequest, auth.getUserEntity());
    }

    /**
     * Function to update the name of a group
     *
     * @param groupid     identifier of a group
     * @param nameRequest object containing the new name of the group
     * @param auth        authentication object of the requesting user
     * @return ResponseEntity<GroupJson>
     * @ApiDog <a href="https://apidog.com/apidoc/project-467959/api-5723995">apiDog documentation</a>
     * @HttpMethod Patch
     * @AllowedRoles teacher
     * @ApiPath /api/groups/{groupid}
     */
    @PatchMapping(ApiRoutes.GROUP_BASE_PATH + "/{groupid}")
    @Roles({UserRole.teacher})
    public ResponseEntity<?> patchGroupName(@PathVariable("groupid") Long groupid, @RequestBody NameRequest nameRequest, Auth auth) {
        return doGroupNameUpdate(groupid, nameRequest, auth.getUserEntity());
    }

    private ResponseEntity<?> doGroupNameUpdate(Long groupid, NameRequest nameRequest, UserEntity user) {

        if (nameRequest.getName() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Name needs to be provided");
        }
        if (nameRequest.getName().isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Name cannot be empty");
        }

        CheckResult<GroupEntity> checkResult = groupUtil.canUpdateGroup(groupid, user);
        if (checkResult.getStatus() != HttpStatus.OK) {
            return ResponseEntity.status(checkResult.getStatus()).body(checkResult.getMessage());
        }
        GroupEntity group = checkResult.getData();

        // Update the group name
        group.setName(nameRequest.getName());

        // Save the changes
        groupRepository.save(group);

        // Return the updated group
        GroupJson groupJson = entityToJsonConverter.groupEntityToJson(group);
        return ResponseEntity.ok(groupJson);
    }

    /**
     * Function to delete a group
     *
     * @param groupid identifier of a group
     * @param auth    authentication object of the requesting user
     * @return ResponseEntity<Void>
     * @ApiDog <a href="https://apidog.com/apidoc/project-467959/api-5723998">apiDog documentation</a>
     * @HttpMethod Delete
     * @AllowedRoles teacher, student
     * @ApiPath /api/groups/{groupid}
     */
    @DeleteMapping(ApiRoutes.GROUP_BASE_PATH + "/{groupid}")
    @Roles({UserRole.teacher, UserRole.student})
    public ResponseEntity<?> deleteGroup(@PathVariable("groupid") Long groupid, Auth auth) {
        CheckResult<GroupEntity> checkResult = groupUtil.canUpdateGroup(groupid, auth.getUserEntity());

        commonDatabaseActions.removeGroup(groupid);
        // Return 204
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Group deleted");
    }

}
