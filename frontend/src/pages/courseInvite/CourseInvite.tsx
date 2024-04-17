import { useParams, Link } from "react-router-dom";
import { Button, Card, Typography } from "antd";
import { AppRoutes } from "../../@types/routes";
import { useTranslation } from "react-i18next"

const { Title } = Typography;

const CourseInvite = () => {
  const { inviteId } = useParams();
  const { t } = useTranslation();

  const courseName = "Introduction to React";
  const courseDescription = "Learn the basics of React development.";

  const handleJoinClick = () => {
    // TODO: Perform the actual POST request here
  };

  return (
    <div style={{padding: "3rem"}}>
      <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center" }}>
      <Title level={2}>{t("courseInvite.confirmJoin")}</Title>
      <Link to={AppRoutes.COURSES}>
        <Button type="primary" onClick={handleJoinClick}>
          {t("courseInvite.join")}
        </Button>
      </Link>
      </div>
      
        <Card title={courseName}>
          <p>{courseDescription}</p>
        </Card>
    </div>
  );
};

export default CourseInvite;
