import { FC, PropsWithChildren, createContext, useEffect, useState } from "react"
import { ApiRoutes, GET_Responses } from "../@types/requests.d"
import apiCall from "../util/apiFetch"
import { useIsAuthenticated } from "@azure/msal-react"

type UserContextProps = {
  user: User | null
  updateUser: () => void
  courses: UserCourseType[] | null
}

export type UserCourseType = GET_Responses[ApiRoutes.USER_COURSES][number]

const UserContext = createContext<UserContextProps>({} as UserContextProps)
export type User = GET_Responses[ApiRoutes.USER]

const UserProvider: FC<PropsWithChildren> = ({ children }) => {
  const isAuthenticated = useIsAuthenticated()
  const [user, setUser] = useState<User | null>(null)
  const [courses, setCourses] = useState<UserCourseType[]|null>(null)

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

        apiCall
          .get(ApiRoutes.USER_COURSES, { 
            id: data.data.id 
          })
          .then((data) => {
            setCourses(data.data)
          })
          .catch((error) => {
            console.error(error)
            // TODO: handle error
          })


      })
      .catch((error) => {
        // TODO: handle error
        console.error(error)
      })
  }

  return <UserContext.Provider value={{ updateUser, user, courses }}>{children}</UserContext.Provider>
}

export { UserProvider, UserContext }