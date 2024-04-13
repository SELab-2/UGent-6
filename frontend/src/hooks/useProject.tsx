import { useContext } from "react"
import { ProjectContext } from "../router/ProjectRoutes"




const useProject = () => {
    const {project} = useContext(ProjectContext)
    return project
}


export default useProject