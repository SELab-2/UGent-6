import { Card, Avatar, theme } from "antd"
import { UserOutlined } from '@ant-design/icons';
import { useTranslation } from "react-i18next"
import { User } from "../../../providers/UserProvider"

const ProfileCard: React.FC<{ user: User }> = ({ user }) => {
  const { token } = theme.useToken()
  const { t } = useTranslation()

  return (
    <Card
      styles={{
        header: {
          background: token.colorPrimaryBg,
        },
        title: {
          fontSize: "1.1em",
        },
      }}
      bordered={false}
      type="inner"
      title={
        <span>
          <Avatar icon={<UserOutlined />} style={{ marginRight: 8 }} />
          {user.name} {user.surname}
        </span>
      }
    >
      {t("profile." + user.role)}
    </Card>
  )
}

export default ProfileCard
