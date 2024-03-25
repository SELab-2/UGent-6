import React  from "react";
import { AppRoutes } from "../../@types/routes"
import {useTranslation} from "react-i18next";
import {Button, Result, Space, Typography} from "antd";


interface ErrorPageProps {
    errorCode?: number;
    errorMessage?: string;
}

const Error: React.FC<ErrorPageProps> = ({ errorCode, errorMessage }) => {
    const { t } = useTranslation()
    const { Title, Text } = Typography;

    let title:string = "";
    let status: "403" | "404" | "500"  = "404";

    // Mapping van errorCode naar status
    const statusMapping: { [key: number]: "403" | "404" | "500" } = {
        403: "403",
        404: "404",
        500: "500"
    };

    // Add default message to error page when no error message was provided
    if(errorMessage === undefined){
        errorMessage = t("error.subtitle");
    }
    // Add default error title if no error code was provided, otherwise set it to error code
    if(errorCode === undefined){
        title = t("error.title")

    }else{
        title = errorCode.toString()
        status = statusMapping[errorCode] || "404"
    }

    return (
        <Result status={status}>
            <Space direction="vertical">
                <Title>{title}</Title>
                <Text>{errorMessage}</Text>
                <Button type="primary" href={AppRoutes.HOME}>{t("error.homepage")}</Button>
            </Space>
        </Result>
    );
}

export default Error;
