import { Card, Typography, Spin } from "antd"
import { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import SubmissionCard from "./components/SubmissionCard";
import { SubmissionType } from "./components/SubmissionCard";

const Submission = () => {
  const { t } = useTranslation()
  const [submission, setSubmission] = useState<SubmissionType | null>(null);

  //TODO: niet met useEffect, maar met een echte submission werken (moet ook een keer updaten voor de docker resultaten)
  useEffect(() => {
    setTimeout(() => {
      setSubmission({
        submissionId: 1,
        project_url: "/projects/1",
        file_url: "1/file",
        group_url: "groups/1",
        structure_accepted: true,
        structure_feedback: "verslag.pdf not found at the root of the zip file",
        docker_results_available: false,
        docker_accepted: false,
        feedback: {
          feedback:"",
          score: 1,
        },
        group: {
          groupId: 1,
          members: [],
          name: ""
        },
        docker_feedback: "",
        submitted_time: "10-03-2023"
      })
      console.log("sethalf")
    }, 250)
    setTimeout(() => {
      setSubmission({
        submissionId: 1,
        project_url: "/projects/1",
        file_url: "1/file",
        group_url: "groups/1",
        structure_accepted: true,
        docker_results_available: true,
        docker_accepted: false,
        feedback: {
          feedback:"",
          score: 1,
        },
        group: {
          groupId: 1,
          members: [],
          name: ""
        },
        
        structure_feedback: "verslag.pdf not found at the root of the zip file",
        docker_feedback: "Test 1:\nSyntax error: unexpected ; at line 218\nTest 2:\nSyntax error: unexpected ; at line 218\nTest 3:\nSyntax error: unexpected ; at line 218\nTest 4:\nSyntax error: unexpected ; at line 218\nTest 5:\nSyntax error: unexpected ; at line 218",
        submitted_time: "10-03-2023"
      })
      console.log("setfull")
    }, 5000)
  }, [])

  if (submission === null) {
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
    <div style={{ margin: "3rem 0" }}>
      <SubmissionCard submission={submission}>

      </SubmissionCard>
    </div>
  )
}

export default Submission
