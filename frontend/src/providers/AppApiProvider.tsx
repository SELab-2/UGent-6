import { Modal } from "antd"
import { HookAPI } from "antd/es/modal/useModal"
import { FC, PropsWithChildren, createContext } from "react"

export type AppApiContextT  = {
  modal: HookAPI,
  
}

export const AppApiContext = createContext<AppApiContextT>({} as AppApiContextT)

const AppApiProvider:FC<PropsWithChildren> = ({ children }) => {
  const [modal, contextHolder] = Modal.useModal()



  return <AppApiContext.Provider value={{modal}}>{children}{contextHolder}</AppApiContext.Provider>
}

export default AppApiProvider