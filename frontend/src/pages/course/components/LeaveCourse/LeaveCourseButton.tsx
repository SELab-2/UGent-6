import React, { FC, useState } from "react";
import {Button, message, Modal, Typography} from "antd";
import { leaveCourse } from "./CourseMembershipService";
import {useTranslation} from "react-i18next";

interface LeaveCourseButtonProps {
    courseId: string;
}

const LeaveCourseButton: FC<LeaveCourseButtonProps> = ({ courseId }) => {
    const { Text } = Typography
    const { t } = useTranslation()
    const [confirmLeaveVisible, setConfirmLeaveVisible] = useState<boolean>(false);

    const handleLeaveConfirm = async () => {
        try {
            const result = await leaveCourse(courseId);
            if (result.success) {
                message.success(result.message); // Using message from useApp
                setTimeout(() => {
                    window.location.href = '/'; // Redirect to home page after leaving course
                }, 2000); // 2 seconds
            } else {
                message.error(result.message); // Using message from useApp
            }
        } catch (error) {
            console.error('Failed to leave the course:');
            message.error('Failed to leave the course'); // Using message from useApp
        }
    };

    const handleLeaveCancel = () => {
        setConfirmLeaveVisible(false);
    };

    const showConfirmLeaveModal = () => {
        setConfirmLeaveVisible(true);
    };

    return (
        <>
            <Button style={{ float: "right", marginTop: "1rem", marginRight: "1rem" }} type="primary" onClick={showConfirmLeaveModal}>
                {t("course.leave")}
            </Button>
            <Modal
                title= {t("course.leave")}
                open={confirmLeaveVisible}
                onOk={handleLeaveConfirm}
                onCancel={handleLeaveCancel}
            >
                <Text> {t("course.leaveConfirm")} </Text>
            </Modal>
        </>
    );
};

export default LeaveCourseButton;
