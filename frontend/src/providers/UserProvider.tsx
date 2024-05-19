import {createContext, FC, PropsWithChildren, useEffect, useState} from "react"
import {ApiRoutes, GET_Responses} from "../@types/requests.d"
import apiCall from "../util/apiFetch"
import {Spin} from "antd"
import useAuth from "../hooks/useAuth";
import {LoginStatus} from "../@types/appTypes";


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
  const auth = useAuth()
  const [user, setUser] = useState<User | null>(null)
  const [courses, setCourses] = useState<UserCourseType[] | null>(null)


  useEffect(() => {
    if (auth.isAuthenticated) {
      updateUser()
    }
  }, [auth])

  const updateCourses = async (userId: number | undefined = user?.id) => {
    if (!userId) return console.error("No user id provided")
    try {
      const res = await apiCall.get(ApiRoutes.USER_COURSES, { id: userId })
      setCourses(res.data)
    } catch (err) {
      // TODO: handle error
    }
  }

  const updateUser = async () => {
    try {
      let data = await apiCall.get(ApiRoutes.USER_AUTH)

      setUser(data.data)

      await updateCourses(data.data.id)
    } catch (err) {
      console.log(err)
    }
  }

  if (!user && (auth.loginStatus === LoginStatus.LOGIN_IN_PROGRESS || auth.loginStatus === LoginStatus.LOGOUT_IN_PROGRESS  || auth.isAuthenticated))
    return (
      <div style={{ display: "flex", justifyContent: "center", alignItems: "center", height: "100vh" }}>
        <Spin size="large" />
      </div>
    )

  return <UserContext.Provider value={{ updateUser, updateCourses, user, courses }}>{children}</UserContext.Provider>
}

export { UserProvider, UserContext }