import React, {FC, useContext, useState} from "react";
import { Button, Modal, Typography } from "antd";
import { leaveCourse } from "./CourseMembershipService";
import { useTranslation } from "react-i18next";
import {useNavigate} from "react-router-dom";
import {AppRoutes} from "../../../../@types/routes";
import useAppApi from "../../../../hooks/useAppApi";
import {LogoutOutlined, UsergroupDeleteOutlined} from "@ant-design/icons";
import {CourseContext} from "../../../../router/CourseRoutes";
import {UserContext} from "../../../../providers/UserProvider";

interface LeaveCourseButtonProps {
    courseId: string;
}

const LeaveCourseButton: FC<LeaveCourseButtonProps> = ({ courseId }) => {
    const navigate = useNavigate();
    const { Text } = Typography;
    const { t } = useTranslation();
    const [confirmLeaveVisible, setConfirmLeaveVisible] = useState<boolean>(false);
    const { message } = useAppApi()
    const { member } = useContext(CourseContext)
    const  userContext  = useContext(UserContext)

    const handleLeaveConfirm = async () => {
        const result = await leaveCourse(courseId, t); // Pass the translation function as a parameter
        if (result.success) {
            message.success(result.message);
            await userContext.updateCourses()
            navigate(AppRoutes.HOME);
        } else {
            message.error(result.message);
            console.error(result);
        }
    };

    const handleLeaveCancel = () => {
        setConfirmLeaveVisible(false);
    };

    const showConfirmLeaveModal = () => {
        setConfirmLeaveVisible(true);
    };

    if(member.relation !== "creator") {
        return (
            <>
                <Button type="text"
                        onClick={showConfirmLeaveModal} icon={<LogoutOutlined/>}>
                    {t("course.leave")}
                </Button>
                <Modal
                    title={t("course.leave")}
                    open={confirmLeaveVisible}
                    onOk={handleLeaveConfirm}
                    onCancel={handleLeaveCancel}
                    okType="danger"
                >
                    <Text> {t("course.leaveConfirm")} </Text>
                </Modal>
            </>
        );
    }
    return null;
};

export default LeaveCourseButton;
