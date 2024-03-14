import useUser from "./useUser"



const useIsTeacher = () => {
  const {user} = useUser()  
  return user?.role === "teacher" || user?.role === "admin"
}

export default useIsTeacher