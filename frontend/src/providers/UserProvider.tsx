import { FC, PropsWithChildren, createContext, useEffect, useState } from "react"
import { ApiRoutes, GET_Responses } from "../@types/requests.d"
import { useIsAuthenticated, useMsal } from "@azure/msal-react"
import { Spin } from "antd"
import { InteractionStatus } from "@azure/msal-browser"
import useApi from "../hooks/useApi"
import { useLocalStorage } from "usehooks-ts"

type UserContextProps = {
  user: User | null
  updateUser: () => Promise<void>
  updateCourses: (userId?: number | undefined) => Promise<void>
  courses: UserCourseType[] | null
}

export type UserCourseType = GET_Responses[ApiRoutes.USER_COURSES][number]

const UserContext = createContext<UserContextProps>({} as UserContextProps)
export type User = GET_Responses[ApiRoutes.USER]

const UserProvider: FC<PropsWithChildren> = ({ children }) => {
  const isAuthenticated = useIsAuthenticated()
  const [user, setUser] = useLocalStorage<User | null>("__user_cache",null)
  const [courses, setCourses] = useLocalStorage<UserCourseType[] | null>("__courses_cache",null)
  const { inProgress } = useMsal()
  const API = useApi()

  useEffect(() => {
    if (isAuthenticated) {
      updateUser()
    } else {
      setUser(null)
    }
  }, [isAuthenticated])

  const updateCourses = async (userId: number | undefined = user?.id) => {
    if (!userId) return console.error("No user id provided")
    const res = await API.GET(ApiRoutes.USER_COURSES, { pathValues: { id: userId } },"page")
    if (!res.success) return setCourses(null)
    setCourses(res.response.data)
  }

  const updateUser = async () => {
      const res = await API.GET(ApiRoutes.USER_AUTH, {}, "page")
      if(!res.success) return setUser(null)
      setUser(res.response.data)

      await updateCourses(res.response.data.id)
  
  }

  if (!user && (!(inProgress === InteractionStatus.Startup || inProgress === InteractionStatus.None || inProgress === InteractionStatus.Logout) || isAuthenticated))
    return (
      <div style={{ display: "flex", justifyContent: "center", alignItems: "center", height: "100vh" }}>
        <Spin size="large" />
      </div>
    )

  return <UserContext.Provider value={{ updateUser, updateCourses, user, courses }}>{children}</UserContext.Provider>
}

export { UserProvider, UserContext }