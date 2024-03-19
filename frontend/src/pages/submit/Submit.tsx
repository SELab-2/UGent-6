import { Card, Typography } from "antd"
import { useTranslation } from "react-i18next"
import SubmitForm from "./components/SubmitForm"
import SubmitStructure from "./components/SubmitStructure"

const Submit = () => {
  const { t } = useTranslation()

  return (
    <div style={{ margin: "3rem 0" }}>
      <Card
        title={t("project.newSubmission")}
        styles={{ body: { display: "flex", justifyContent: "center",width:"100%" } }}
      >
        <div style={{display: "inline-block"}}>
          <Typography.Title level={3}>{t("project.structure")}</Typography.Title>
   
          <SubmitStructure structure="test" />
          <br/> <br/>
      
          <SubmitForm />
        </div>
      </Card>
    </div>
  )
}

export default Submit
