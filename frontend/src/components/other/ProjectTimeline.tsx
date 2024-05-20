import { FC, useMemo } from "react"
import { Timeline, Typography } from "antd"
import { ProjectType } from "../../pages/project/Project"
import { useTranslation } from "react-i18next"
import { Link } from "react-router-dom"
import { AppRoutes } from "../../@types/routes"
import { ProjectStatus } from "../../@types/requests"
import i18n from "i18next"

const colorByProjectStatus: Record<ProjectStatus, string> = {
  "correct": "green",
  "incorrect": "red",
  "not started": "gray",
  "no group": "warning"
}

const ProjectTimeline: FC<{ projects: ProjectType[] | null }> = ({ projects }) => {
  const { t } = useTranslation()

  const items = useMemo(() => {
    if (projects === null) return null

    const now = new Date();
    const filteredAndSortedProjects = projects
        .filter(project => new Date(project.deadline) > now)
        .sort((a, b) => {
          const deadlineA = new Date(a.deadline).getTime();
          const deadlineB = new Date(b.deadline).getTime();
          return deadlineA - deadlineB;
        });

    return filteredAndSortedProjects.map((p) => ({
      label: new Date(p.deadline).toLocaleString(i18n.language, {
        year: "numeric",
        month: "long",
        day: "numeric",
        hour: "2-digit",
        minute: "2-digit",
      }),
      children: <Link to={AppRoutes.PROJECT.replace(":courseId", p.course.courseId + "").replace(":projectId", p.projectId + "")}>{p.name}</Link>,
      color: colorByProjectStatus[p.status ?? "not started"],
    }))
  }, [projects])

  return (
      <>
        {items !== null && items.length === 0 && <Typography.Text type="secondary">{t("home.projects.noProjects")}</Typography.Text>}
        <Timeline
            pending={items === null ? t("home.fetching") : null}
            mode="left"
            items={items ?? []}
        />
      </>
  )
}

export default ProjectTimeline
