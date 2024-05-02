import { List, Dropdown, Space, Modal } from "antd"
import { DownOutlined } from "@ant-design/icons"
import { useTranslation } from "react-i18next"
import { UserRole } from "../../../@types/requests"
import { useState } from "react"
import { UsersType } from "../EditRole"
import { GET_Responses, ApiRoutes } from "../../../@types/requests.d"
import { User } from "../../../providers/UserProvider"

//this is ugly, but if I put this in GET_responses, it will be confused with the User type (and there's no GET request with this as a response).
//this is also the only place this is used, so I think it's fine.
export type UsersListItem = { name: string, surname: string, id: number, url: string, email: string, role: UserRole }

const UserList: React.FC<{ users: UsersType; updateRole: (user: UsersListItem, role: UserRole) => void }> = ({ users, updateRole }) => {
  const { t } = useTranslation()
  const [visible, setVisible] = useState(false)
  const [selectedUser, setSelectedUser] = useState<UsersListItem | null>(null)
  const [selectedRole, setSelectedRole] = useState<UserRole | null>(null)

  const handleMenuClick = (user: UsersListItem, role: UserRole) => {
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

  //sort based on name, then surname, then email
  const sortedUsers = [...users].sort((a, b) => {
    const nameComparison = a.name.localeCompare(b.name);
    if (nameComparison !== 0) return nameComparison;

    const surnameComparison = a.surname.localeCompare(b.surname);
    if (surnameComparison !== 0) return surnameComparison;

    return a.email.localeCompare(b.email);
  });

  const renderUserItem = (user: UsersListItem) => (
    <List.Item>
      <List.Item.Meta title={user.name + " " + user.surname} description={user.email} />
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
        dataSource={sortedUsers}
        renderItem={renderUserItem}
        locale={{ emptyText: t("editRole.noUsersFound") }}
      />
      <div>
        <Modal
          title={t("editRole.confirmation")}
          open={visible}
          onOk={handleConfirm}
          onCancel={onCancel}
        >
          {t("editRole.confirmationText",{role: selectedRole, name: selectedUser?.name + " " + selectedUser?.surname})}
        </Modal>
      </div>
    </div>
  )
}

export default UserList
