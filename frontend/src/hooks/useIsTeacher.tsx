import useUser from "./useUser"



const useIsTeacher = () => {
  const {user} = useUser()  
  console.log("====>",user);
  return user?.role === "teacher" || user?.role === "admin"
}

export default useIsTeacher