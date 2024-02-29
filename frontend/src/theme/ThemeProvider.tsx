import { ConfigProvider, ThemeConfig } from "antd"
import { FC, PropsWithChildren } from "react"
import useApp from "../hooks/useApp"
import {darkTheme} from "./themes/dark"
import {lightTheme} from "./themes/light"
import { Themes } from "../@types/types"
import { dodonaTheme } from "./themes/dodona"

const appThemes:Record<Themes,ThemeConfig> = {
  light: lightTheme,
  dark: darkTheme,
  dodona: dodonaTheme
}

const ThemeProvider: FC<PropsWithChildren> = ({ children }) => {
  const {theme} = useApp()
  const selectedTheme = appThemes[theme] ?? appThemes.dark

  
  return <ConfigProvider theme={selectedTheme}>
    {children}
  </ConfigProvider>
}

export default ThemeProvider
