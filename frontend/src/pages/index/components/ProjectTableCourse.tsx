import { Button, Table, TableProps, Tag, Tooltip } from "antd"
import { FC, useMemo } from "react"
import { ApiRoutes, GET_Responses } from "../../../@types/requests.d"
import { useTranslation } from "react-i18next"
import i18n from 'i18next'
import useAppApi from "../../../hooks/useAppApi"
import ProjectStatusTag from "./ProjectStatusTag"
import GroupProgress from "./GroupProgress"
import { Link } from "react-router-dom"
import { AppRoutes } from "../../../@types/routes"
import {ClockCircleOutlined, EyeInvisibleOutlined, EyeOutlined} from "@ant-design/icons"
import useIsCourseAdmin from "../../../hooks/useIsCourseAdmin";

export type ProjectType = GET_Responses[ApiRoutes.PROJECT]

const ProjectTableCourse: FC<{ projects: ProjectType[] | null, ignoreColumns?: string[] }> = ({ projects, ignoreColumns }) => {
    const { t } = useTranslation()
    const { modal } = useAppApi()
    const isCourseAdmin = useIsCourseAdmin()

    const columns: TableProps<ProjectType>["columns"] = useMemo(
        () => {
            let columns: TableProps<ProjectType>["columns"] = [
                {
                    title: t("home.projects.name"),
                    key: "name",
                    render: (project: ProjectType) => (
                        <Link to={AppRoutes.PROJECT.replace(":courseId", project.course.courseId + "").replace(":projectId", project.projectId + "")}>
                            <Button
                                type="link"
                                style={{ fontWeight: "bold" }}
                            >
                                {project.name}
                            </Button>
                        </Link>
                    )
                },
                {
                    title: t("home.projects.course"),
                    dataIndex: "course",
                    key: "course",
                    sorter: (a: ProjectType, b: ProjectType) => a.course.name.localeCompare(b.course.name),
                    sortDirections: ['ascend', 'descend'],
                    render: (course: ProjectType["course"]) => course.name
                },
                {
                    title: t("home.projects.deadline"),
                    dataIndex: "deadline",
                    key: "deadline",
                    sorter: (a: ProjectType, b: ProjectType) => new Date(a.deadline).getTime() - new Date(b.deadline).getTime(),
                    sortDirections: ['ascend', "descend"],
                    defaultSortOrder: "ascend",
                    filters: [{ text: t('home.projects.deadlineNotPassed'), value: 'notPassed' }],
                    onFilter: (value: any, record: any) => {
                        const currentTimestamp = new Date().getTime();
                        const deadlineTimestamp = new Date(record.deadline).getTime();
                        return value === 'notPassed' ? deadlineTimestamp >= currentTimestamp : true;
                    },
                    render: (text: string) =>
                        new Date(text).toLocaleString(i18n.language, {
                            year: "numeric",
                            month: "long",
                            day: "numeric",
                            hour: "2-digit",
                            minute: "2-digit",
                        }),
                },
                {
                    title: t("home.projects.groupProgress"),
                    key: "progress",
                    render: (project: ProjectType) => (
                        <GroupProgress
                            usersCompleted={project.progress.completed}
                            userCount={project.progress.total}
                        />
                    ),
                }
            ]

            if (ignoreColumns) {
                columns = columns.filter((c) => !ignoreColumns.includes(c.key as string))
            }

            if (isCourseAdmin) {
                columns = columns.filter((c) => c.key !== "status")
                columns.push({
                    title: t("home.projects.visibility"),
                    key: "visible",
                    render: (project: ProjectType) => {
                        if (project.visible) {
                            return <Tag color="blue" icon={<EyeOutlined/>}>{t("home.projects.visibleStatus.visible")}</Tag>
                        } else if (project.visibleAfter) {
                            return (
                                <Tooltip title={`${t("home.projects.visibleStatus.visibleFrom")} ${new Date(project.visibleAfter).toLocaleString(i18n.language, {
                                    year: "numeric",
                                    month: "long",
                                    day: "numeric",
                                    hour: "2-digit",
                                    minute: "2-digit",
                                })}`}>
                                    <Tag icon={<ClockCircleOutlined />} color="default">{t("home.projects.visibleStatus.scheduled")}</Tag>
                                </Tooltip>
                            )
                        } else {
                            return <Tag color="default" icon={<EyeInvisibleOutlined/>}>{t("home.projects.visibleStatus.invisible")}</Tag>
                        }
                    }
                })
            } else {
                columns.push({
                    title: t("home.projects.projectStatus"),
                    key: "status",
                    render: (project: ProjectType) =>
                        project.status && <ProjectStatusTag status={project.status} />,
                })
            }

            return columns
        },
        [t, modal, projects, isCourseAdmin]
    )

    return (
        <Table
            showSorterTooltip={{ mouseEnterDelay: 1 }}
            locale={{
                emptyText: t("home.projects.noProjects"),
            }}
            loading={projects == null}
            dataSource={projects ?? []}
            columns={columns}
            rowKey={(project) => project.projectId}
        />
    )
}

export default ProjectTableCourse
