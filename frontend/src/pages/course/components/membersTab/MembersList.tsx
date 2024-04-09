import { Button, List, Popconfirm, Radio, Select, Tooltip } from "antd"
import { FC } from "react"
import { useTranslation } from "react-i18next"
import { UserDeleteOutlined } from "@ant-design/icons"
import { CourseMemberType } from "./MemberCard"


const rolesNames = {
  course_admin: "Teacher",
  enrolled: "Student",
  creator: "Admin",
}

const MembersList: FC<{ members: CourseMemberType[]|null }> = ({ members }) => {
  const { t } = useTranslation()

  const removeUserFromCourse = (userId: number) => {
    //TODO: make request
  }

  const onRoleChange = (userId: number, role: string) => {
    // TODO: make request

  }

  return (
    <List
    loading={members === null}
      dataSource={members??[]}
      renderItem={(user) => (
        <List.Item
          className="show-actions-on-hover"
          actions={[
            <Radio.Group onChange={(e) => onRoleChange(user.userId, e.target.value)} key="role" value={user.relation} buttonStyle="solid">
              <Radio.Button value="creator">Admin</Radio.Button>
              <Radio.Button value="course_admin">Teacher</Radio.Button>
              <Radio.Button value="enrolled">Student</Radio.Button>
            </Radio.Group>,


            <Popconfirm
              title={t("course.removeUserConfirmTitle")}
              description={t("course.removeUserConfirm",{
                name: user.name
              
              })}
              onConfirm={() => removeUserFromCourse(user.userId)}
              okText={t("course.yes")}
              cancelText={t("course.cancel")}
              key="remove"
            >
              <Tooltip placement="left" title={t("course.removeFromCourse", { name: user.name })}>
                <Button
                  danger
                  type="primary"
                  key="remove"
                  icon={<UserDeleteOutlined />}
                />
              </Tooltip>
            </Popconfirm>,
          ]}
        >
          <List.Item.Meta title={`${user.name} ${user.surname}`} description={rolesNames[user.relation]} />
        </List.Item>
      )}
    />
  )
}

export default MembersList
