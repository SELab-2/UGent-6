import { useParams, Link } from "react-router-dom";
import { Button, Card, Typography } from "antd";

const { Title } = Typography;

const CourseInvite = () => {
  const { inviteId } = useParams();

  const courseName = "Introduction to React";
  const courseDescription = "Learn the basics of React development.";

  const handleJoinClick = () => {
    // TODO: Perform the actual POST request here
  };

  return (
    <div style={{ padding: "20px" }}>
      <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center" }}>
      <Title level={2}>Do you want to join this course?</Title>
      <Link to="/courses">
        <Button type="primary" onClick={handleJoinClick}>
          Join Course
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
