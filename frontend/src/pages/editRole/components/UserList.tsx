import { List, Dropdown, Space, Modal, Menu } from 'antd';
import { DownOutlined } from '@ant-design/icons';
import { useTranslation } from "react-i18next"
import { Users } from '../EditRole';
import { ApiRoutes, GET_Responses } from '../../../@types/requests';
import { MouseEventHandler, useState, useRef } from 'react';

type User = GET_Responses[ApiRoutes.GROUP_MEMBER]

const UserList: React.FC<{ users: Users, updateRole: (user: User, role: String) => void }> = ({ users, updateRole }) => {
	const { t } = useTranslation();
	const [visible, setVisible] = useState(false);
	const [selectedUser, setSelectedUser] = useState<User | null>(null);
    const [selectedRole, setSelectedRole] = useState<string | null>(null);

    const handleMenuClick = (user: User, role: string) => {
        setSelectedUser(user);
        setSelectedRole(role);
        setVisible(true);
    };

    const handleConfirm = () => {
        setVisible(false);
		if (selectedUser === null || selectedRole === null) return;
        updateRole(selectedUser, selectedRole);
    };

    const onCancel = () => {
        setVisible(false);
    };

	const renderUserItem = (user: User) => (
		<List.Item>
		<List.Item.Meta
		  title={user.name + " " + user.surname}
		/>
		<Dropdown
                trigger={['click']}
                placement="bottomRight"
                getPopupContainer={() => document.body}
				menu={{items: [
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
						}
					],
					selectable: true,
					defaultSelectedKeys: [user.role],
					onClick: (e) => handleMenuClick(user, e.key)
				}}
            >
		<a onClick={(e) => e.preventDefault()}>
		  <Space>
			{t("editRole." + user.role)}
			<DownOutlined/>
		  </Space>
		</a>
	  </Dropdown>
	  </List.Item>
	  );
	
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
				{t("editRole.confirmationText").replace("{{role}}", t("editRole." + selectedRole)).replace("{{name}}", selectedUser?.name + " " + selectedUser?.surname)}
			</Modal>
		</div>
	</div>
  );
};

export default UserList;
