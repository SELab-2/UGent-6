import { FC, PropsWithChildren, createContext, useEffect, useState } from "react"
import { ApiRoutes, GET_Responses } from "../@types/requests.d"
import apiCall from "../util/apiFetch"
import { useIsAuthenticated } from "@azure/msal-react"
import { Spin } from "antd"

type UserContextProps = {
  user: User | null
  updateUser: () => Promise<void>
  updateCourses: (userId?:number|undefined) => Promise<void>
  courses: UserCourseType[] | null
}

export type UserCourseType = GET_Responses[ApiRoutes.USER_COURSES][number]

const UserContext = createContext<UserContextProps>({} as UserContextProps)
export type User = GET_Responses[ApiRoutes.USER]

const UserProvider: FC<PropsWithChildren> = ({ children }) => {
  const isAuthenticated = useIsAuthenticated()
  const [user, setUser] = useState<User | null>(null)
  const [courses, setCourses] = useState<UserCourseType[] | null>(null)

  useEffect(() => {
    if (isAuthenticated) {
      updateUser()
    }
  }, [isAuthenticated])

  const updateCourses =  async (userId:number|undefined = user?.id) => {
    if(!userId) return console.error("No user id provided")
    try {
      const res = await apiCall.get(ApiRoutes.USER_COURSES,{id:userId})
      setCourses(res.data)

    } catch(err){
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


  if (!isAuthenticated) return <div style={{ display: "flex", justifyContent: "center", alignItems: "center", height: "100%" }}>
    <Spin size="large" />
  </div>

  return <UserContext.Provider value={{ updateUser,updateCourses, user, courses }}>{children}</UserContext.Provider>
}

export { UserProvider, UserContext }
