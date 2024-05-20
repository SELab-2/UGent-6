import { useTranslation } from "react-i18next";
import { Button } from "antd";
import useAuth from "../../../hooks/useAuth";
import {useNavigate} from "react-router-dom";
import {BACKEND_SERVER} from "../../../util/backendServer";

const UnauthNav = () => {
  const { t } = useTranslation();
  const auth = useAuth();
  const navigate = useNavigate();
  const handleLogin = async () => {
    try {
      await auth.login()
      window.location.replace(BACKEND_SERVER + "/web/auth/signin")
    } catch (error) {
      console.error(error)
    }
  }

  return (
      <Button size="large" type="primary" onClick={handleLogin}>{t("nav.login")}</Button>
  )
}

export default UnauthNav
