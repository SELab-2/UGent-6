import { Affix, Button, Card, Col, Form, Row, Typography } from "antd"
import { useTranslation } from "react-i18next"
import SubmitForm from "./components/SubmitForm"
import SubmitStructure from "./components/SubmitStructure"
import { useNavigate } from "react-router-dom"
import React, { useState, useRef} from 'react';


const Submit = () => {
  const { t } = useTranslation()
  const [form] = Form.useForm()

  const navigate = useNavigate()

  // file upload system
  const [selectedFile, setSelectedFile] = useState<File | undefined>(undefined);
  const fileInputRef = useRef<HTMLInputElement>(null);

  const handleFileChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    setSelectedFile(event.target.files?.[0]);
  }

  const handleFileUpload = async () => {
    if (!selectedFile){
      return alert("Please select a file to upload");
    }

    const formData = new FormData();
    formData.append("file", selectedFile as Blob); // Blob atm ma mss is er iets da het moet zijn voor de backend

    const response = await fetch('https://selab2-6.ugent.be/api/submissions/submit', { // juiste url nog toevoegen en body info enzo
      method: 'POST',
      body: formData,
    });
    
    if (response.ok) {
      alert("File uploaded successfully");
    } else {
      alert("Failed to upload file");
    }
      
  }
  return (
    <>
      <div>
        <Row
          style={{ marginTop: "3rem" }}
          gutter={[32, 32]}
        >
          <Col
            md={16}
            sm={24}
            xs={24}
          >
            <Card
              title={t("project.files")}
              style={{ height: "100%" }}
              styles={{ body: { height:"100%" } }}
            >
              <SubmitForm form={form} />
            </Card>
          </Col>

          <Col
            md={8}
            sm={24}
            xs={24}
          >
            <Card
              title={t("project.structure")}
              style={{ height: "100%" }}
              styles={{ body: { display: "flex", justifyContent: "center" } }}
            >
              <SubmitStructure structure="test" />
            </Card>
          </Col>
        </Row>
        <Row>
        <Card
        style={{  width: "100%", maxWidth: "1200px", height: "4rem", margin: "1rem" }}
        styles={{ body: { padding:"10px 0",display:"flex",gap:"1rem" } }}
      >
        <Button size="large" onClick={() => navigate(-1)}>
          {t("goBack")}
        </Button>
        <Button
          type="primary"
          size="large"
          style={{ width: "100%", height: "100%" }}
          disabled={!form.isFieldsTouched()}
          onClick={()=> form.submit()}
        >
          {t("project.submit")}
        </Button>
      </Card>
        </Row>
      </div>
      
    </>
  )
}

export default Submit
