import { Trans, useTranslation } from "react-i18next";
import { msalInstance } from "../../index"

const UnauthNav = () => {
  const { t } = useTranslation();
  const handleLogin = async () => {
    try {
      await msalInstance.loginPopup();
    } catch (error) {
      console.error(error)
    }
  }

  return (
    <div style={{float:"right"}}>
      <button onClick={handleLogin}>{t("navBar.login")}</button>
    </div>
  )
}

export default UnauthNav
