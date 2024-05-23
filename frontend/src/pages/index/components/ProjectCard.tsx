import { FC, useEffect, useState } from "react"
import ProjectTableCourse, { ProjectType } from "./ProjectTableCourse"
import ProjectTable, { ProjectType as NormalProjectType } from "./ProjectTable"
import { Button, Card } from "antd"
import { ApiRoutes } from "../../../@types/requests.d"
import { useTranslation } from "react-i18next"
import { AppRoutes } from "../../../@types/routes"
import { useNavigate } from "react-router-dom"
import CourseAdminView from "../../../hooks/CourseAdminView"
import { PlusOutlined } from "@ant-design/icons"
import useApi from "../../../hooks/useApi"
import useIsCourseAdmin from "../../../hooks/useIsCourseAdmin";

const ProjectCard: FC<{ courseId?: number }> = ({ courseId }) => {
    const [projects, setProjects] = useState<ProjectType[] | null>(null)
    const { t } = useTranslation()
    const navigate = useNavigate()
    const API = useApi()
    const isCourseAdmin = useIsCourseAdmin()

    useEffect(() => {
        if (courseId) {
            API.GET(ApiRoutes.COURSE_PROJECTS, { pathValues: { id: courseId } }).then((res) => {
                if (!res.success) return
                setProjects(res.response.data)
            })
        }
    }, [courseId])

    return (
        <>
            {isCourseAdmin && (
                <CourseAdminView>
                    <div style={{ textAlign: "right", paddingBottom: "10px" }}>
                        <Button
                            onClick={() => navigate(AppRoutes.PROJECT_CREATE.replace(":courseId", String(courseId)))}
                            icon={<PlusOutlined />}
                            type="primary"
                        >
                            {t("project.newProject")}
                        </Button>
                    </div>
                </CourseAdminView>
            )}
            <Card
                style={{
                    width: "100%",
                    overflow: "auto",
                }}
                styles={{
                    body: {
                        padding: "0",
                    },
                }}
            >
                {isCourseAdmin ? (
                    <ProjectTableCourse
                        ignoreColumns={["course"] }
                        projects={projects}
                    />
                ) : (
                    <ProjectTable
                        noFilter
                        ignoreColumns={["course"] }
                        projects={projects as NormalProjectType[]}
                    />
                )}
            </Card>
        </>
    )
}

export default ProjectCard
