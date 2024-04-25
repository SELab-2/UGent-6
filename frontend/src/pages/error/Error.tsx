import React from "react";
import { useTranslation } from "react-i18next";
import { Button, Result, Space, Typography } from "antd";
import { useNavigate } from "react-router-dom";
import { AxiosError } from "axios";

interface ErrorPageProps {
    errorCode?: number;
    errorMessage?: string;
    axiosError?: AxiosError;
}

const Error: React.FC<ErrorPageProps> = ({ errorCode, errorMessage, axiosError }) => {
    const { t } = useTranslation();
    const { Title, Text } = Typography;
    const navigate = useNavigate();

    let title: string
    let status: "403" | "404" | "500" = "404";

    // Mapping van errorCode naar status
    const statusMapping: { [key: number]: "403" | "404" | "500" } = {
        403: "403",
        404: "404",
        500: "500"
    };

    const textMapping: { [key: number]: string } = {
        404: t("error.404_message"),
        403: t("error.403_message"),
        500: t("error.500_message")
    };

    // If axiosError was provided, put error Info into the variables
    if(axiosError){
        errorCode = axiosError.response?.status || undefined;
        if(axiosError.response !== undefined){
            errorMessage = axiosError.response.data?.toString() || undefined
        }
    }

    // add default message to error page when no error message was provided
    if (errorMessage === undefined) {
        if (errorCode !== undefined) {
            errorMessage = textMapping[errorCode] || t("error.subtitle");
        } else {
            errorMessage = t("error.subtitle");
        }
    }
    // Add default error title if no error code was provided, otherwise set it to error code
    if (errorCode === undefined) {
        title = t("error.title");
    } else {
        title = errorCode.toString();
        status = statusMapping[errorCode] || "404";
    }

    return (
        <Result status={status}>
            <Space direction="vertical">
                <Title>{title}</Title>
                <Text>{errorMessage}</Text>

                <Button onClick={() => navigate(-1)} type="primary">{t("error.back")}</Button>
            </Space>
        </Result>
    );
};

export default Error;
