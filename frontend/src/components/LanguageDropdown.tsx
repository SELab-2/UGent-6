import { GlobalOutlined } from "@ant-design/icons"
import { Dropdown, Typography } from "antd"
import { Language } from "../@types/appTypes"
import useApp from "../hooks/useApp"
import { MenuProps } from "antd/lib"

const items: MenuProps["items"] = [
  {
    key: Language.EN,
    label: "English",
  },
  {
    key: Language.NL,
    label: "Nederlands",
  },
]


const LanguageDropdown = () => {
  const app = useApp()

  const languageChange: MenuProps["onClick"] = (props) => {
    app.setLanguage(props.key as Language)
  }



  return <Dropdown menu={{ items, onClick: languageChange }}>
    <Typography.Text style={{ cursor: "pointer", width: "6rem" }}>
      <GlobalOutlined /> {app.language}
    </Typography.Text>
  </Dropdown>
}

export default LanguageDropdown