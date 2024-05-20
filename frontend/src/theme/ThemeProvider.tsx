import { ConfigProvider, ThemeConfig } from "antd"
import { FC, PropsWithChildren } from "react"
import useApp from "../hooks/useApp"
import {darkTheme} from "./themes/dark"
import {lightTheme} from "./themes/light"
import { Language, Themes } from "../@types/appTypes"
import nlNL from 'antd/locale/nl_NL';
import enUS from 'antd/locale/en_US';


const i18n_locale = {
  [Language.NL]: nlNL,
  [Language.EN]: enUS
}

const appThemes:Record<Themes,ThemeConfig> = {
  light: lightTheme,
  dark: darkTheme,
}

const ThemeProvider: FC<PropsWithChildren> = ({ children }) => {
  const {theme,language} = useApp()
  const selectedTheme = appThemes[theme] ?? appThemes.dark

  
  return <ConfigProvider theme={selectedTheme} locale={i18n_locale[language]}>
    {children}
  </ConfigProvider>
}

export default ThemeProvider
