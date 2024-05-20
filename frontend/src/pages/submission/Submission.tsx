import { Spin } from "antd"
import { useEffect, useRef, useState } from "react"
import SubmissionCard from "./components/SubmissionCard"
import { SubmissionType } from "./components/SubmissionCard"
import { useParams } from "react-router-dom"
import { ApiRoutes } from "../../@types/requests.d"
import useApi from "../../hooks/useApi"

const Submission = () => {
  const [submission, setSubmission] = useState<SubmissionType | null>(null)
  const { submissionId } = useParams()
  const API = useApi()
  const timeoutRef = useRef<NodeJS.Timeout | null>(null)

  const fetchSubmission = () => {
    if (!submissionId) return console.error("No submissionId found");

    API.GET(ApiRoutes.SUBMISSION, { pathValues: { id: submissionId } }).then((res) => {
      if (!res.success) return;
      setSubmission(res.response.data);

      // If dockerStatus is "running", schedule the next API call after 1 second
      if (res.response.data.dockerStatus === "running") {
        timeoutRef.current = setTimeout(fetchSubmission, 2500);
      }
    });
  };

  useEffect(() => {
    fetchSubmission();

    // Clear the timeout when the component is unmounted
    return () => {
      if (timeoutRef.current) {
        clearTimeout(timeoutRef.current);
      }
    };
  }, [submissionId]);

  if (submission === null) {
    return (
      <div style={{ width: "100%", height: "100%", display: "flex", justifyContent: "center", alignItems: "center" }}>
        <Spin
          tip="Loading..."
          size="large"
        >
          <span> </span>
        </Spin>
      </div>
    )
  }

  return (
    <div style={{ margin: "3rem 0",marginTop:"1rem" }}>
      <SubmissionCard submission={submission} />
    </div>
  )
}

export default Submission
