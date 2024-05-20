import { useTranslation } from "react-i18next";
import { Button } from "antd";
import useAuth from "../../../hooks/useAuth";
import {useNavigate} from "react-router-dom";

const UnauthNav = () => {
  const { t } = useTranslation();
  const auth = useAuth();
  const navigate = useNavigate();
  const handleLogin = async () => {
    try {
      await auth.login()
      window.location.replace("http://localhost:3000/web/auth/signin")
    } catch (error) {
      console.error(error)
    }
  }

  return (
      <Button size="large" type="primary" onClick={handleLogin}>{t("nav.login")}</Button>
  )
}

export default UnauthNav
