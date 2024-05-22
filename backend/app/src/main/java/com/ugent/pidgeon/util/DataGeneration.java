package com.ugent.pidgeon.util;

import com.ugent.pidgeon.controllers.CourseController;
import com.ugent.pidgeon.model.Auth;
import com.ugent.pidgeon.model.json.CourseJson;
import com.ugent.pidgeon.model.json.CourseMemberRequestJson;
import com.ugent.pidgeon.model.json.CourseWithInfoJson;
import com.ugent.pidgeon.model.json.RelationRequest;
import com.ugent.pidgeon.postgre.models.CourseEntity;
import com.ugent.pidgeon.postgre.models.UserEntity;
import com.ugent.pidgeon.postgre.models.types.CourseRelation;
import com.ugent.pidgeon.postgre.models.types.UserRole;
import com.ugent.pidgeon.postgre.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class DataGeneration {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CourseController courseController;


    public void generate(Auth auth) {
        makeFakeUsersAndCourses(auth);
    }

    private void makeFakeUsersAndCourses(Auth auth) {
        String[] vaktitels = {"Computationele Biologie", "Computer Architectuur", "Mobile and Broadband Access Networks",
        "Wiskundige modelering", "Design of Multimedia Applications"};
        for (int i = 0; i < 5; i++) {
            UserEntity user = new UserEntity(
                    "teacher",
                    "number ".concat(String.valueOf(i)),
                    "teacher.number".concat(String.valueOf(i)).concat("@ugent.be"),
                    UserRole.teacher,
                    "azure_id_number_teacher_".concat(String.valueOf(i)),
                    "teacher".concat(String.valueOf(i * 1000))
            );
            userRepository.save(user);
            CourseJson cj = new CourseJson(
                    vaktitels[i],
                    "# VAK\n Dit vak gaat over vanalles.",
                    false,
                    2023
            );
            ResponseEntity<?> resp = courseController.createCourse(cj, auth);
            if (resp.getStatusCode().is2xxSuccessful()) {
                CourseMemberRequestJson cmrj = new CourseMemberRequestJson();
                if (i == 0 || i >= 3) {
                    cmrj.setRelation(CourseRelation.creator.toString());
                } else {
                    cmrj.setRelation(CourseRelation.course_admin.toString());
                }
                cmrj.setUserId(user.getId());
                CourseWithInfoJson course = (CourseWithInfoJson) resp.getBody();
                if (course != null) {
                    courseController.addCourseMember(auth, course.courseId(), cmrj);
                    RelationRequest rr = new RelationRequest();
                    if (i >= 3) {
                        rr.setRelation(CourseRelation.enrolled.toString());
                    } else if (i > 0) {
                        rr.setRelation(CourseRelation.creator.toString());
                    } else {
                        rr.setRelation(CourseRelation.course_admin.toString());
                    }
                    courseController.updateCourseMember(auth, course.courseId(), rr, auth.getUserEntity().getId());
                    cmrj.setRelation(CourseRelation.enrolled.toString());
                    for (int j = 0; j < 10; j++) {
                        UserEntity u = getStudentUserEntity(i, j);
                        userRepository.save(u);
                        cmrj.setUserId(u.getId());
                        courseController.addCourseMember(auth, course.courseId(), cmrj);
                    }
                }
            }
        }
    }

    private void makeProjects(Auth auth, CourseWithInfoJson course) {

    }

    private static UserEntity getStudentUserEntity(int i, int j) {
        int idx = i *10 + j;
        return new UserEntity(
                "student",
                "number ".concat(String.valueOf(idx)),
                "student.number".concat(String.valueOf(idx)).concat("@ugent.be"),
                UserRole.student,
                "azure_id_number_".concat(String.valueOf(idx)),
                String.valueOf(idx * 1000)
        );
    }

}
