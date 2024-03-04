import { FC, PropsWithChildren, createContext, useEffect, useState } from "react"
import { Language, Themes } from "../@types/types"
import { useTranslation } from "react-i18next"

export type AppContextT = {
  theme: Themes
  language: Language
  setTheme: (theme: Themes) => void
  setLanguage: (language: Language) => void
}

const AppContext = createContext<AppContextT>({} as AppContextT)

const AppProvider: FC<PropsWithChildren> = ({ children }) => {
  const { i18n } = useTranslation()

  const [theme, setTheme] = useState<Themes>(Themes.DARK)
  const [language, setLanguage] = useState<Language>(Language.NL)

  useEffect(() => {
    const localTheme = (window.localStorage.getItem("theme") as Themes) ?? Themes.DARK
    const localLanguage =( window.localStorage.getItem("i18n") as Language) ?? Language.NL
    setTheme(localTheme)
    setLanguage(localLanguage)
    i18n.changeLanguage(localLanguage)

    document.body.classList.add(localTheme + "-theme")

  }, [])

  const handleSetTheme = (theme: Themes) => {
    setTheme(theme)
    document.body.classList.remove(Themes.LIGHT + "-theme", Themes.DARK + "-theme", Themes.DODONA + "-theme")
    document.body.classList.add(theme + "-theme")
    window.localStorage.setItem("theme", theme)
  }

  const handleSetLanguage = (language: Language) => {
    setLanguage(language)
    window.localStorage.setItem("i18n", language)
    i18n.changeLanguage(language)

  }

  return <AppContext.Provider value={{ theme, language, setTheme: handleSetTheme, setLanguage: handleSetLanguage }}>{children}</AppContext.Provider>
}

export { AppProvider, AppContext }
