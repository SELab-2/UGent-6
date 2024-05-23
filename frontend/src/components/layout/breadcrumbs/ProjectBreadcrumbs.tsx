import { HomeFilled } from "@ant-design/icons"
import { Breadcrumb, BreadcrumbItemProps, BreadcrumbProps } from "antd"
import { FC, useContext, useMemo } from "react"
import { ProjectType } from "../../../pages/index/components/ProjectTableCourse"
import { Link, useLocation, useMatch } from "react-router-dom"
import useCourse from "../../../hooks/useCourse"
import { AppRoutes } from "../../../@types/routes"
import { useTranslation } from "react-i18next"
import { UserContext } from "../../../providers/UserProvider"

const ProjectBreadcrumbs: FC<{ project: ProjectType | null }> = ({ project }) => {
  const course = useCourse()
  const { courses } = useContext(UserContext)
  const { t } = useTranslation()
  const matchProject = useMatch(AppRoutes.PROJECT)
  const submitMatch = useMatch(AppRoutes.NEW_SUBMISSION)
  const submissionMatch = useMatch(AppRoutes.SUBMISSION)
  const editProjectMatch = useMatch(AppRoutes.EDIT_PROJECT)

  const location = useLocation()
  const index = new URLSearchParams(location.search).get("index")


  const items: BreadcrumbProps["items"] = useMemo(() => {
    const menuItems: BreadcrumbItemProps["menu"] = {
      items:
        courses?.map((c) => ({
          key: c.courseId,
          title: <Link to={AppRoutes.COURSE.replace(":courseId", c.courseId + "")}>{c.name}</Link>,
        })) ?? [],
    }

    return [
      {
        title: (
          <Link to={AppRoutes.HOME}>
            <HomeFilled />
          </Link>
        ),
      },
      {
        title: <Link to={AppRoutes.COURSE.replace(":courseId", course.courseId + "")}>{course.name}</Link>,
        menu: menuItems,
      },
    ]
  }, [courses, course])

  let breadcrumbs = [...items]
  if (breadcrumbs) {
    if (matchProject && project) {
      breadcrumbs.push({
        title: project.name,
      })
    } else {
      breadcrumbs.push({
        title: <Link to={AppRoutes.PROJECT.replace(":courseId", course.courseId + "").replace(":projectId", project?.projectId + "")}>{project?.name || ""}</Link>,
      })

      if (submitMatch) {
        breadcrumbs.push({
          title: t("project.breadcrumbs.submit"),
        })
      }

      if (submissionMatch) {
        breadcrumbs.push({
          title: t("project.breadcrumbs.submission") + (index ? ` #${index}` : ""),
        })
      }
    }

    if (editProjectMatch) {
      breadcrumbs.push({
        title: t("project.breadcrumbs.editPage"),
      })

      return (
        <div style={{ display: "flex", justifyContent: "center", width: "100%" }}>
          <div style={{ maxWidth: "700px", width: "100%" }}>
            <Breadcrumb
              style={{ marginTop: "1rem" }}
              items={breadcrumbs}
            />
          </div>
        </div>
      )
    }
  }

  return (
    <Breadcrumb
      style={{ marginTop: "1rem" }}
      items={breadcrumbs}
    />
  )
}

export default ProjectBreadcrumbs
