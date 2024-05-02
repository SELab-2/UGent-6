import { Button, Dropdown, List, Popconfirm, Radio, Select, Space, Tooltip } from "antd"
import { FC, useContext } from "react"
import { useTranslation } from "react-i18next"
import { DownOutlined, UserDeleteOutlined } from "@ant-design/icons"
import { CourseMemberType } from "./MemberCard"
import useIsCourseAdmin from "../../../../hooks/useIsCourseAdmin"
import { MenuProps } from "antd/lib"
import { ApiRoutes, CourseRelation } from "../../../../@types/requests.d"
import useUser from "../../../../hooks/useUser"
import { CourseContext } from "../../../../router/CourseRoutes"
import apiCall from "../../../../util/apiFetch"
import { useParams } from "react-router-dom"

const MembersList: FC<{ members: CourseMemberType[] | null; onChange: (members: CourseMemberType[]) => void }> = ({ members, onChange }) => {
  const { t } = useTranslation()
  const isCourseAdmin = useIsCourseAdmin()
  const relation = useContext(CourseContext).member.relation
  const { courseId } = useParams()

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
    //TODO: test this
    const req = await apiCall.delete(ApiRoutes.COURSE_MEMBER, undefined, { userId, courseId })
    console.log(req.data)
    const newMembers = members?.filter((m) => m.user.userId !== userId)
    onChange(newMembers ?? [])
  }

  const onRoleChange = async (userId: number, role: CourseRelation) => {
    // TODO: test this 
    if(!courseId) return
    const response = await apiCall.patch(ApiRoutes.COURSE_MEMBER, {relation: role}, { userId, courseId })
    console.log(response.data);
    onChange(response.data)
    
  }

  return (
    <List
      locale={{ emptyText: t("course.noMembersFound") }}
      loading={members === null}
      dataSource={members ?? []}
      renderItem={(u) => (
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
                title={u.user.userId === user?.id ? "" : t("course.removeFromCourse", { name: u.user.name })}
              >
                <Button
                  danger
                  key="remove"
                  disabled={u.user.userId === user?.id && relation === "creator"}
                  icon={<UserDeleteOutlined />}
                />
              </Tooltip>
            </Popconfirm>,
          ]}
        >
          <List.Item.Meta
            title={u.user.name}
            description={
              isCourseAdmin ? (
                <Dropdown
                  disabled={u.user.userId === user?.id}
                  menu={{ items, onClick: (e) => onRoleChange(u.user.userId, e.key as CourseRelation), defaultSelectedKeys: [u.relation] }}
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
      )}
    />
  )
}

export default MembersList
