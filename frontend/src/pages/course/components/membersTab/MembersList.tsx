import { Button, Dropdown, List, Popconfirm, Space, Tooltip } from "antd"
import { FC, useContext } from "react"
import { useTranslation } from "react-i18next"
import { DownOutlined, UserDeleteOutlined } from "@ant-design/icons"
import { CourseMemberType } from "./MemberCard"
import { MenuProps } from "antd/lib"
import { ApiRoutes, CourseRelation } from "../../../../@types/requests.d"
import useUser from "../../../../hooks/useUser"
import { CourseContext } from "../../../../router/CourseRoutes"
import { useParams } from "react-router-dom"
import useApi from "../../../../hooks/useApi"

const MembersList: FC<{ members: CourseMemberType[] | null; onChange: (members: CourseMemberType[]) => void }> = ({ members, onChange }) => {
  const { t } = useTranslation()
  const relation = useContext(CourseContext).member.relation
  const { courseId } = useParams()
  const API = useApi()
  const { user } = useUser()

  const items: MenuProps["items"] = [
    {
      key: "creator",
      label: t("editRole.teacher"),
      disabled: true,
      style: { display: "none" },
    },
    {
      key: "course_admin",
      label: t("editRole.course_admin"),
    },
    {
      key: "enrolled",
      label: t("editRole.student"),
    },
  ]

  const rolesNames = {
    creator: t("editRole.teacher"),
    course_admin: t("editRole.course_admin"),
    enrolled: t("editRole.student"),
  }

  const removeUserFromCourse = async (userId: number) => {
    if (!courseId) return
    const req = await API.DELETE(ApiRoutes.COURSE_MEMBER, { pathValues: { userId, courseId } }, "message")
    if (!req.success) return

    const newMembers = members?.filter((m) => m.user.userId !== userId)
    onChange(newMembers ?? [])
  }

  const onRoleChange = async (userId: number, role: CourseRelation) => {
    if (!courseId) return
    const response = await API.PATCH(ApiRoutes.COURSE_MEMBER, { body: { relation: role }, pathValues: { userId, courseId } }, "message")
    if (!response.success) return

    const newMembers = members?.map((m) => {
      if (m.user.userId === userId) return { user: m.user, relation: role }
      return m
    })

    onChange(newMembers ?? [])
  }

  return (
    <List
      locale={{ emptyText: t("course.noMembersFound") }}
      loading={members === null}
      dataSource={members ?? []}
      renderItem={(u) => {
        
        let disableRemove = u.user.userId === user?.id && relation === "creator" || u.relation !== "enrolled" && relation !== "creator"

        return (
        <List.Item
          actions={[
            <Popconfirm
              title={t("course.removeUserConfirmTitle")}
              description={t("course.removeUserConfirm", {
                name: u.user.name,
              })}
              onConfirm={() => removeUserFromCourse(u.user.userId)}
              okText={t("course.yes")}
              cancelText={t("course.cancel")}
              key="remove"
            >
              <Tooltip
                placement="left"
                title={!disableRemove ?  u.user.userId === user?.id ? "" : t("course.removeFromCourse", { name: u.user.name }): ""}
              >
                <Button
                  danger
                  key="remove"
                  disabled={disableRemove}
                  icon={<UserDeleteOutlined />}
                />
              </Tooltip>
            </Popconfirm>,
          ]}
        >
          <List.Item.Meta
            title={u.user.name}
            description={
              relation === "creator" ? (
                <Dropdown
                  disabled={u.user.userId === user?.id}
                  menu={{ items, onClick: (e) => onRoleChange(u.user.userId, e.key as CourseRelation), selectedKeys: [u.relation] }}
                >
                  <a onClick={(e) => e.preventDefault()}>
                    <Space>
                      {rolesNames[u.relation]}
                      <DownOutlined />
                    </Space>
                  </a>
                </Dropdown>
              ) : (
                rolesNames[u.relation]
              )
            }
          />
        </List.Item>
      )}}
    />
  )
}

export default MembersList
