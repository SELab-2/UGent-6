import i18next from "i18next"
import { initReactI18next } from "react-i18next"


import translation_en from "./en/translation.json"
import translation_nl from "./nl/translation.json"

i18next.use(initReactI18next).init({
  debug: false,
  resources: {
    en: {
      translation: translation_en,
    },
    nl: {
      translation: translation_nl,
    },
  },
  // if you see an error like: "Argument of type 'DefaultTFuncReturn' is not assignable to parameter of type xyz"
  // set returnNull to false (and also in the i18next.d.ts options)
  // returnNull: false,
})
