import { createContext, useEffect, useState } from "react"
import { ProjectType } from "../pages/project/Project"
import { Outlet, useParams } from "react-router-dom"
import apiCall from "../util/apiFetch"
import { ApiRoutes } from "../@types/requests.d"
import useApi from "../pages/index/components/useApi"

type ProjectContextType = {
  project: ProjectType | null
}

export const ProjectContext = createContext<ProjectContextType>({} as ProjectContextType)

const ProjectRoutes = () => {
  const [project, setProject] = useState<ProjectContextType["project"]>(null)
  const { projectId } = useParams()
  const { GET } = useApi()

  useEffect(() => {
    // TODO make api call `projectId`
    if (!projectId) return console.error("ProjectId is not defined")




    let ignore = false
    console.log("Making the request", projectId)

    GET(ApiRoutes.PROJECT, { pathValues: { id: projectId! } }, "page").then((res) => {
      if (res.success && !ignore) setProject(res.response.data)
    })

    return () => {
      ignore = true
    }
  }, [projectId])

  return (
    <ProjectContext.Provider value={{ project }}>
      <Outlet />
    </ProjectContext.Provider>
  )
}

export default ProjectRoutes
