import { Modal, message } from "antd"
import { MessageInstance } from "antd/es/message/interface"
import { HookAPI } from "antd/es/modal/useModal"
import { FC, PropsWithChildren, createContext } from "react"

export type AppApiContextT  = {
  modal: HookAPI,
  message: MessageInstance
}

export const AppApiContext = createContext<AppApiContextT>({} as AppApiContextT)

const AppApiProvider:FC<PropsWithChildren> = ({ children }) => {
  const [modal, modalContextHolder] = Modal.useModal()
  const [messageApi, messageContextHolder] = message.useMessage();



  return <AppApiContext.Provider value={{modal,message}}>{children}{modalContextHolder}{messageContextHolder}</AppApiContext.Provider>
}

export default AppApiProvider