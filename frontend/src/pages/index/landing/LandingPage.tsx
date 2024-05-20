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
import { motion } from "framer-motion"
import useAuth from "../../../hooks/useAuth";
import {useNavigate} from "react-router-dom";

const defaultTransition = { duration: 0.5, ease: [0.44, 0, 0.56, 1], type: "tween" }

const defaultIconTransition = { damping: 30, mass: 1, stiffness: 400, type: "spring" }
const defaultAnimate = { opacity: 1, y: 0, scale: 1, rotate: 0 }
const defaultInitial = { opacity: 0.001, y: 64 }

const LandingPage: FC = () => {
  const { t } = useTranslation()
  const auth = useAuth()
  const navigate = useNavigate()
  const handleLogin = async () => {
    try {
      await auth.login()
      window.location.replace("http://localhost:3000/web/auth/signin")
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
          <motion.div
            initial={defaultInitial}
            animate={defaultAnimate}
            transition={defaultTransition}
          >
            <Typography.Title
              style={{ fontWeight: "700", fontSize: "3.5em", padding: 0, margin: 0, marginBottom: "0.5rem", zIndex: 3, color: "white" }}
              level={1}
            >
              {t("landingPage.title-1")} <span style={{ color: "#1E64C8" }}>UGent</span> {t("landingPage.title-2")}
            </Typography.Title>
          </motion.div>

          <motion.div
            initial={defaultInitial}
            animate={defaultAnimate}
            transition={{ ...defaultTransition, delay: 0.08 }}
          >
            <Typography.Title style={{ maxWidth: "700px", color: "#B2CFE2", padding: 0, margin: 0, fontSize: "2em", zIndex: 3 }}>{t("landingPage.subtitle-1")} </Typography.Title>
          </motion.div>

          <motion.div
            initial={defaultInitial}
            animate={defaultAnimate}
            transition={{ ...defaultTransition, delay: 0.16 }}
          >
            <Typography.Title style={{ maxWidth: "700px", color: "#B2CFE2", padding: 0, margin: 0, fontSize: "2em", zIndex: 3 }}>{t("landingPage.subtitle-2")} </Typography.Title>
          </motion.div>

          <br />
          <motion.div
            initial={defaultInitial}
            animate={defaultAnimate}
            transition={{ ...defaultTransition, delay: 0.24 }}
          >
            <button
              className="landing-page-btn"
              onClick={handleLogin}
            >
              {t("landingPage.getStarted")}
            </button>
          </motion.div>

          <div style={{ maxWidth: "700px" }}>
            <motion.div
              className="ugent-logo logo-item "
              initial={{ opacity: 0.001, scale: 0.2, rotate: -70 }}
              animate={defaultAnimate}
              transition={{ ...defaultIconTransition, delay: 0.64 }}
            >
              <img src={ugentLogo} />
            </motion.div>
            <motion.div
              className="code-logo logo-item "
              initial={{ opacity: 0.001, scale: 0.2, rotate: -70 }}
              animate={defaultAnimate}
              transition={{ ...defaultIconTransition, delay: 0.48 }}
            >
            <img
              src={codeLogo}
            />
            </motion.div>
            <motion.div
              className="js-logo logo-item "
              initial={{ opacity: 0.001, scale: 0.2, rotate: -70 }}
              animate={defaultAnimate}
              transition={{ ...defaultIconTransition, delay: 0.40 }}
            >
            <img
              src={jsLogo}
            />
            </motion.div>
            <motion.div
              className="docker-logo logo-item "
              initial={{ opacity: 0.001, scale: 0.2, rotate: -70 }}
              animate={defaultAnimate}
              transition={{ ...defaultIconTransition, delay: 0.56 }}
            >
            <img
              src={dockerLogo}
            />
            </motion.div>
            <motion.div
              className="py-logo logo-item "
              initial={{ opacity: 0.001, scale: 0.2, rotate: -70 }}
              animate={defaultAnimate}
              transition={{ ...defaultIconTransition, delay: 0.72 }}
            >
            <img
              src={pythonLogo}
            />
            </motion.div>
            <motion.div
              className="c-logo logo-item "
              initial={{ opacity: 0.001, scale: 0.2, rotate: -70 }}
              animate={defaultAnimate}
              transition={{ ...defaultIconTransition, delay: 0.8 }}
            >
            <img
              src={cLogo}
            />
            </motion.div>
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
