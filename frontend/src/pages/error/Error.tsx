import React, { useContext }  from "react";
import { AppRoutes } from "../../@types/routes"
import {useTranslation} from "react-i18next";
import {Button, Result, Space, Typography} from "antd";
import { Link, useNavigate } from "react-router-dom";
import { ErrorContext } from "../../providers/ErrorProvider";


interface ErrorPageProps {
    errorCode?: number;
    errorMessage?: string;
}

const Error: React.FC<ErrorPageProps> = ({ errorCode, errorMessage }) => {
    const { t } = useTranslation()
    const { setError} = useContext(ErrorContext) 
    const { Title, Text } = Typography;
    const navigate = useNavigate()

    let title:string = "";
    let status: "403" | "404" | "500"  = "404";

    // Mapping van errorCode naar status
    const statusMapping: { [key: number]: "403" | "404" | "500" } = {
        403: "403",
        404: "404",
        500: "500"
    };

    const textMapping: { [key: number]: string} = {
        404: t("error.404_message"),
        403: t("error.403_message"),
        500: t("error.500_message")
    }

    // Add default message to error page when no error message was provided
    if(errorMessage === undefined){
        if(errorCode !== undefined){
            errorMessage = textMapping[errorCode] || t("error.subtitle");
        }else{
            errorMessage = t("error.subtitle");
        }
    }
    // Add default error title if no error code was provided, otherwise set it to error code
    if(errorCode === undefined){
        title = t("error.title")

    }else{
        title = errorCode.toString()
        status = statusMapping[errorCode] || "404"
    }

    const goBack = () => {
        setError(null)
        navigate(-1)
    }

    return (
        <Result status={status}>
            <Space direction="vertical">
                <Title>{title}</Title>
                <Text>{errorMessage}</Text>
               
                <Button onClick={goBack} type="primary" >{t("error.homepage")}</Button>
            </Space>
        </Result>
    );
}

export default Error;
