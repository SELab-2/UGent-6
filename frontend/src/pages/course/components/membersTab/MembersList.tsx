import { Button, Dropdown, List, Popconfirm, Radio, Select, Space, Tooltip } from "antd"
import { FC } from "react"
import { useTranslation } from "react-i18next"
import { DownOutlined, UserDeleteOutlined } from "@ant-design/icons"
import { CourseMemberType } from "./MemberCard"
import useIsCourseAdmin from "../../../../hooks/useIsCourseAdmin"
import { MenuProps } from "antd/lib"
import { CourseRelation } from "../../../../@types/requests"

const items: MenuProps["items"] = [
  {
    key: "creator",
    label: "Admin",
  },
  {
    key: "course_admin",
    label: "Teacher",
  },
  {
    key: "enrolled",
    label: "Student",
  },
]

const rolesNames = {
  course_admin: "Teacher",
  enrolled: "Student",
  creator: "Admin",
}

const MembersList: FC<{ members: CourseMemberType[] | null }> = ({ members }) => {
  const { t } = useTranslation()
  const isCourseAdmin = useIsCourseAdmin()

  const removeUserFromCourse = (userId: number) => {
    //TODO: make request
  }

  const onRoleChange = (userId: number, role: CourseRelation) => {
    // TODO: make request
  }

  const rel = "course_admin"
  return (
    <List
      loading={members === null}
      dataSource={members ?? []}
      renderItem={(user) => (
        <List.Item
          actions={[
         

            <Popconfirm
              title={t("course.removeUserConfirmTitle")}
              description={t("course.removeUserConfirm", {
                name: user.name,
              })}
              onConfirm={() => removeUserFromCourse(user.id)}
              okText={t("course.yes")}
              cancelText={t("course.cancel")}
              key="remove"
            >
              <Tooltip
                placement="left"
                title={t("course.removeFromCourse", { name: user.name })}
              >
                <Button
                  danger
                  key="remove"
                  icon={<UserDeleteOutlined />}
                />
              </Tooltip>
            </Popconfirm>,
          ]}
        >
          <List.Item.Meta
            title={user.name}
            description={
              isCourseAdmin ? (
                <Dropdown menu={{ items,onClick:(e) => onRoleChange(user.id, e.key as CourseRelation), defaultSelectedKeys:[rel] }}>
                  <a onClick={(e) => e.preventDefault()}>
                    <Space>
                      {rolesNames[rel]}
                      <DownOutlined />
                    </Space>
                  </a>
                </Dropdown>
              ) : (
                rolesNames[rel]
              )
            }
          />
        </List.Item>
      )}
    />
  )
}

export default MembersList
