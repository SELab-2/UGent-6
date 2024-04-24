import React, { FC, useState } from "react";
import { Button, Modal, Typography } from "antd";
import { leaveCourse } from "./CourseMembershipService";
import { useTranslation } from "react-i18next";
import {useNavigate} from "react-router-dom";
import {AppRoutes} from "../../../../@types/routes";
import useAppApi from "../../../../hooks/useAppApi";

interface LeaveCourseButtonProps {
    courseId: string;
}

const LeaveCourseButton: FC<LeaveCourseButtonProps> = ({ courseId }) => {
    const navigate = useNavigate();
    const { Text } = Typography;
    const { t } = useTranslation();
    const [confirmLeaveVisible, setConfirmLeaveVisible] = useState<boolean>(false);
    const { message } = useAppApi()

    const handleLeaveConfirm = async () => {
        const result = await leaveCourse(courseId, t); // Pass the translation function as a parameter
        if (result.success) {
            message.success(result.message);
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

    return (
        <>
            <Button style={{ float: "right", marginTop: "1rem", marginRight: "1rem" }} type="primary" onClick={showConfirmLeaveModal}>
                {t("course.leave")}
            </Button>
            <Modal
                title={t("course.leave")}
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
