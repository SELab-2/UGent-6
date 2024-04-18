import { useTranslation } from "react-i18next";
import { msalInstance } from "../../../index"
import { Button } from "antd";

const UnauthNav = () => {
  const { t } = useTranslation();
  const handleLogin = async () => {
    try {
      await msalInstance.loginPopup({
        scopes: ['openid', 'profile', 'User.Read'],
      });
      
    } catch (error) {
      console.error(error)
    }
  }

  return (
      <Button size="large" type="primary" onClick={handleLogin}>{t("nav.login")}</Button>
  )
}

export default UnauthNav
