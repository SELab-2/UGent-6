import { Card, Avatar, theme, Button } from "antd"
import { Link } from "react-router-dom"
import { UserOutlined } from '@ant-design/icons';
import { useTranslation } from "react-i18next"
import { User } from "../../../providers/UserProvider"
import { AppRoutes } from "../../../@types/routes";
import AdminView from "../../../hooks/AdminView";

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
      type="inner"
      title={
        <span>
          <Avatar icon={<UserOutlined />} style={{ marginRight: 8 }} />
          {user.name} {user.surname}
          
        </span>
      }
    >
      {t("profile." + user.role)}
      <Link to={AppRoutes.EDIT_ROLE}>
        <AdminView>
          <Button type="primary" style={{ float: "right" }}>
            {t("profile.editRole")}
          </Button>
        </AdminView>
      </Link>
    </Card>
  )
}

export default ProfileCard
