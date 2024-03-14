import { FC, PropsWithChildren, createContext, useEffect, useState } from "react"
import { ApiRoutes, GET_Responses } from "../@types/requests.d"
import apiCall from "../util/apiFetch"
import { useIsAuthenticated } from "@azure/msal-react"

type UserContextProps = {
  user: User | null
  updateUser: () => void
}

const UserContext = createContext<UserContextProps>({} as UserContextProps)
export type User = GET_Responses[ApiRoutes.USER]

const UserProvider: FC<PropsWithChildren> = ({ children }) => {
  const isAuthenticated = useIsAuthenticated()
  const [user, setUser] = useState<User | null>(null)

  useEffect(() => {
    if (isAuthenticated) {
      updateUser()
    }
  }, [isAuthenticated])

  const updateUser = () => {
    apiCall
      .get(ApiRoutes.USER_AUTH)
      .then((data) => {
        setUser(data.data)
      })
      .catch((error) => {
        // TODO: handle error
        console.error(error)
      })
  }

  return <UserContext.Provider value={{ updateUser, user }}>{ children}</UserContext.Provider>
}

export { UserProvider, UserContext }
