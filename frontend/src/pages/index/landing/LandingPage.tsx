import { FC } from "react"

import image from "../../../assets/blueblob.webp"
import { Typography } from "antd"
import Navbar from "./Navbar"
import ugentLogo from "../../../assets/landingPageLogos/ugentLogo.png"
import cLogo from "../../../assets/landingPageLogos/cLogo.png"
import pythonLogo from "../../../assets/landingPageLogos/pythonLogo.png"
import jsLogo from "../../../assets/landingPageLogos/jsLogo.png"
import dockerLogo from "../../../assets/landingPageLogos/dockerLogo.png"
import codeLogo from "../../../assets/landingPageLogos/codeLogo.png"
import { useTranslation } from "react-i18next"
import { msalInstance } from "../../.."

const LandingPage: FC = () => {
  const { t } = useTranslation()
  
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
    <div
      className="landing-page"
      style={{ background: "black", width: "100vw", height: "100vh", position: "fixed", display: "flex", justifyContent: "center" }}
    >
      <div style={{ maxWidth: "1200px", height: "100%", width: "100%" }}>
        <Navbar onLogin={handleLogin} />
        <div
          className="landing-title"
          style={{ textAlign: "start", height: "100%", marginTop: "8rem" }}
        >
          <Typography.Title
            style={{ fontWeight: "700", fontSize: "3em", padding: 0, margin: 0, marginBottom: "0.5rem", zIndex: 3,color:"white" }}
            level={1}
          >
            {t("landingPage.title-1")} <span style={{ color: "#1E64C8" }}>UGent</span> {t("landingPage.title-2")}
          </Typography.Title>
          <Typography.Title style={{ maxWidth: "700px", color: "#B2CFE2", padding: 0, margin: 0, fontSize: "2em", zIndex: 3 }}>{t("landingPage.subtitle")} </Typography.Title>
          <br />
          <button className="landing-page-btn" onClick={handleLogin}>{t("landingPage.getStarted")}</button>
          <div style={{ maxWidth: "700px" }}>
            <img
              className="ugent-logo"
              src={ugentLogo}
            />
            <img
              className="code-logo"
              src={codeLogo}
            />
            <img
              className="js-logo"
              src={jsLogo}
            />
            <img
              className="docker-logo"
              src={dockerLogo}
            />
            <img
              className="py-logo"
              src={pythonLogo}
            />
            <img
              className="c-logo"
              src={cLogo}
            />
          </div>
        </div>

        <img
          className="blob-image"
          src={image}
        />
      </div>
    </div>
  )
}

export default LandingPage
