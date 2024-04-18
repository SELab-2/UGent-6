import { List, Dropdown, Space, Modal } from "antd"
import { DownOutlined } from "@ant-design/icons"
import { useTranslation } from "react-i18next"
import { UserRole } from "../../../@types/requests"
import { useState } from "react"
import { UsersType } from "../EditRole"

const UserList: React.FC<{ users: UsersType[]; updateRole: (user: UsersType, role: UserRole) => void }> = ({ users, updateRole }) => {
  const { t } = useTranslation()
  const [visible, setVisible] = useState(false)
  const [selectedUser, setSelectedUser] = useState<UsersType | null>(null)
  const [selectedRole, setSelectedRole] = useState<UserRole | null>(null)

  const handleMenuClick = (user: UsersType, role: UserRole) => {
    setSelectedUser(user)
    setSelectedRole(role)
    setVisible(true)
  }

  const handleConfirm = () => {
    setVisible(false)
    if (selectedUser === null || selectedRole === null) return
    updateRole(selectedUser, selectedRole)
  }

  const onCancel = () => {
    setVisible(false)
  }

  const renderUserItem = (user: UsersType) => (
    <List.Item>
      <List.Item.Meta title={user.name + " " + user.surname} />
      <Dropdown
        trigger={["click"]}
        placement="bottomRight"
        menu={{
          items: [
            {
              key: "student",
              label: t("editRole.student"),
            },
            {
              key: "teacher",
              label: t("editRole.teacher"),
            },
            {
              key: "admin",
              label: t("editRole.admin"),
            },
          ],
					selectedKeys: [user.role],
          onClick: (e) => handleMenuClick(user, e.key as UserRole),
        }}
      >
        <a onClick={(e) => e.preventDefault()}>
          <Space>
            {t("editRole." + user.role)}
            <DownOutlined />
          </Space>
        </a>
      </Dropdown>
    </List.Item>
  )

  return (
    <div>
      <List
        itemLayout="horizontal"
        dataSource={users}
        renderItem={renderUserItem}
      />
      <div>
        <Modal
          title={t("editRole.confirmation")}
          open={visible}
          onOk={handleConfirm}
          onCancel={onCancel}
        >
          {t("editRole.confirmationText",{role: selectedRole, name: selectedUser?.name + " " + selectedUser?.surname })}
        </Modal>
      </div>
    </div>
  )
}

export default UserList
