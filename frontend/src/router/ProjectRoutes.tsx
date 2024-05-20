import { createContext, useEffect } from "react"
import { ProjectType } from "../pages/project/Project"
import { Outlet, useParams } from "react-router-dom"
import { ApiRoutes } from "../@types/requests.d"
import useApi from "../hooks/useApi"
import { useSessionStorage } from "usehooks-ts"
import ProjectBreadcrumbs from "../components/layout/breadcrumbs/ProjectBreadcrumbs"

type ProjectContextType = {
  project: ProjectType | null
  updateProject: (project: ProjectType) => void
}

export const ProjectContext = createContext<ProjectContextType>({} as ProjectContextType)

const ProjectRoutes = () => {
  const { projectId } = useParams()
  const [project, setProject] = useSessionStorage<ProjectContextType["project"]>("__project_cache_" + projectId, null)
  const { GET } = useApi()

  useEffect(() => {
    // TODO make api call `projectId`
    if (!projectId) return console.error("ProjectId is not defined")

    let ignore = false

    GET(ApiRoutes.PROJECT, { pathValues: { id: projectId! } }, "page").then((res) => {
      if (ignore) return
      if (res.success) setProject(res.response.data)
      else setProject(null)
    })

    return () => {
      ignore = true
    }
  }, [projectId])

  const updateProject = (project: ProjectType) => {
    setProject(project)
  }

  return (
    <ProjectContext.Provider value={{ project, updateProject }}>
      <ProjectBreadcrumbs project={project} />
      <Outlet />
    </ProjectContext.Provider>
  )
}

export default ProjectRoutes
