import { Typography } from "antd"
import { useNavigate } from "react-router-dom"
import LanguageDropdown from "../../../components/LanguageDropdown"
import { FC } from "react"
import {useTranslation} from "react-i18next";

const Navbar: FC<{ onLogin: () => void }> = ({ onLogin }) => {
  const navigate = useNavigate()
  const { t } = useTranslation()

  return (
    <div style={{ height: "6rem", display: "flex", justifyContent: "space-between", margin: "2rem" }}>
      <div>
        <Typography.Title
          style={{ padding: 0, margin: 0, cursor: "pointer", color: "white" }}
          level={4}
          onClick={() => navigate("/")}
        >
          Pigeonhole
        </Typography.Title>
      </div>

      <div style={{ display: "flex", gap: "2rem", alignItems: "start" }}>
        <div style={{ transform: "translateY(8px)" }} className="white-color">
          <LanguageDropdown />
        </div>
        <div>
          <button
            className="landing-page-btn"
            onClick={onLogin}
          >
            {t("nav.login")}
          </button>
        </div>
      </div>
    </div>
  )
}

export default Navbar
