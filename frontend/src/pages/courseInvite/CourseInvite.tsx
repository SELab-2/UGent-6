import { useParams, Link, useNavigate } from "react-router-dom";
import { Button, Card, Typography, Spin } from "antd";
import { AppRoutes } from "../../@types/routes";
import { useTranslation } from "react-i18next"
import {useEffect, useState} from "react";
import apiCall from "../../util/apiFetch";
import {ApiRoutes} from "../../@types/requests.d";

const { Title } = Typography;

const CourseInvite = () => {
  const { inviteId } = useParams();
  const { t } = useTranslation();
  const navigate = useNavigate();

  const [courseName, setCourseName] = useState<string | null>(null);
  const [courseDescription, setCourseDescription] = useState<string | null>(null);

  useEffect(() => {
    if (!inviteId) return console.error("No inviteId or courseKey found")
    apiCall.get(ApiRoutes.COURSE_JOIN, { id: inviteId }).then((res) => {
      console.log(res.data);
      setCourseName(res.data.name);
      setCourseDescription(res.data.description);
    })
  }, [])

  const handleJoinClick = () => {
    //POST request to join the course
    if (!inviteId) return console.error("No inviteId or courseKey found")
    apiCall.post(ApiRoutes.COURSE_JOIN.replace(":id", inviteId), {}).then((res) => {
      console.log(res.data);
      if (res.data === "success") {
        navigate(AppRoutes.COURSES);
      } else {
        //TODO: mss vervangen met iets mooiers
        window.alert("Failed to join course:" + res.data);
      }
    })
  };

  if (courseName === null || courseDescription === null) {
    return (
      <div style={{ width: "100%", height: "100%", display: "flex", justifyContent: "center", alignItems: "center" }}>
        <Spin
          tip="Loading..."
          size="large"
        />
      </div>
    )
}

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
