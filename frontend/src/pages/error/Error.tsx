import React  from "react";
import { AppRoutes } from "../../@types/routes"
import {useTranslation} from "react-i18next";
import {Button, Space, Typography} from "antd";

interface ErrorPageProps {
    errorCode?: number;
    errorMessage?: string;
}

const Error: React.FC<ErrorPageProps> = ({ errorCode, errorMessage }) => {
    const { t } = useTranslation()
    const { Title, Text } = Typography;

    let title:string = "";

    // Add default message to error page when no error message was provided
    if(errorMessage === undefined){
        errorMessage = t("error.subtitle");
    }
    // Add default error title if no error code was provided, otherwise set it to error code
    if(errorCode === undefined){
        title = t("error.title")
    }else{
        title = errorCode.toString()
    }

    return (
        <div>
            <Space direction="vertical">
                <Title>{title}</Title>
                <Text>{errorMessage}</Text>
                <Button type="primary" href={AppRoutes.HOME}>{t("error.homepage")}</Button>
            </Space>
        </div>
    );
}

export default Error;
