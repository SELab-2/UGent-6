import React  from "react";
import { Link } from "react-router-dom";
import {useTranslation} from "react-i18next";

interface ErrorPageProps {
    errorCode?: number;
    errorMessage?: string;
}

const Error: React.FC<ErrorPageProps> = ({ errorCode, errorMessage }) => {
    const { t } = useTranslation()
    return (
        <div>
            <h1>{t("error.title")} {errorCode}</h1>
            <p>{t("error.subtitle")}</p>
            <p>{errorMessage}</p>
            <Link to="/">Go to Homepage</Link>
        </div>
    );
}

export default Error;
