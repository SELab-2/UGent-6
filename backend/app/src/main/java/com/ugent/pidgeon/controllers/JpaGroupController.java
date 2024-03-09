package com.ugent.pidgeon.controllers;

import com.ugent.pidgeon.postgre.models.GroupEntity;
import com.ugent.pidgeon.postgre.models.GroupFeedbackEntity;
import com.ugent.pidgeon.postgre.models.SubmissionEntity;
import com.ugent.pidgeon.postgre.models.UserEntity;
import com.ugent.pidgeon.postgre.repository.GroupFeedbackRepository;
import com.ugent.pidgeon.postgre.repository.GroupRepository;
import com.ugent.pidgeon.postgre.repository.SubmissionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
public class JpaGroupController {

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private GroupFeedbackRepository groupFeedbackRepository;

    @Autowired
    SubmissionRepository submissionRepository;

    @GetMapping("/api/temp/groups")
    public List<String> getGroups() {
        List<String> res = new ArrayList<>();
        for (GroupEntity group : groupRepository.findAll()) {
            StringBuilder groupString = new StringBuilder();
            groupString.append(group.getName()).append("-with users: ");
            for (UserEntity user : groupRepository.findCourseUsersByGroupId(group.getId())) {
                groupString.append(user.getName()).append(", ");
            }
            List<Long> projectIds = groupRepository.findProjectsByGroupId(group.getId());
            groupString.append("-with grades: ");
            for (long projectId : projectIds) {
                GroupFeedbackEntity feedback = groupFeedbackRepository.findByGroupIdAndProjectId(group.getId(), projectId);
                groupString.append(feedback.getGrade()).append(", ");
            }
            groupString.append("-with submissions: ");
            for (long projectId : projectIds) {
                for (SubmissionEntity submission : submissionRepository.findByGroupIdAndProjectId(group.getId(), projectId)) {
                    groupString.append(submission.getSubmissionTime()).append(", ");
                }
            }
            groupString.append("|");
            res.add(groupString.toString());
        }
        return res;
    }
}
