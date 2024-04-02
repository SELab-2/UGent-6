import { Card, Col, Row, Space, Tooltip, theme } from "antd"
import { ApiRoutes, GET_Responses } from "../../@types/requests"
import Markdown from "react-markdown"
import { Prism as SyntaxHighlighter } from "react-syntax-highlighter"
import { oneDark, oneLight } from "react-syntax-highlighter/dist/esm/styles/prism"
import useApp from "../../hooks/useApp"
import { useTranslation } from "react-i18next"
import { useParams } from "react-router-dom"
import SubmissionCard from "./components/SubmissionCard"
import useCourse from "../../hooks/useCourse"
import GroupCard from "./components/GroupCard"
import useProject from "../../hooks/useProject"
import ScoreCard from "./components/ScoreCard"
import CourseEnrolledView from "../../hooks/CourseEnrolledView"
import CourseAdminView from "../../hooks/CourseAdminView"
import SubmissionsCard from "./components/SubmissionsCard"

//  dracula, darcula,oneDark,vscDarkPlus  | prism, base16AteliersulphurpoolLight, oneLight

export type ProjectType = GET_Responses[ApiRoutes.PROJECT]

const Project = () => {
  const { token } = theme.useToken()
  const { t } = useTranslation()
  const app = useApp()
  const course = useCourse()
  const { projectId } = useParams()
  const project = useProject()

  const CodeBlock = {
    code({ children, className, node, ...rest }: any) {
      const match = /language-(\w+)/.exec(className || "")
      return match ? (
        <SyntaxHighlighter
          {...rest}
          PreTag="div"
          children={String(children).replace(/\n$/, "")}
          language={match[1]}
          style={app.theme === "light" ? oneLight : oneDark}
        />
      ) : (
        <code
          {...rest}
          className={className}
        >
          {children}
        </code>
      )
    },
  }

  // if (!project) {
  //   return (
  //     <div style={{ display: "flex", justifyContent: "center", alignItems: "center", height: "100vh" }}>
  //       <Spin size="large" />
  //     </div>
  //   )
  // }
  const now = Date.now()
  const deadline = new Date(project?.deadline ?? "").getTime()
  return (
    <div style={{ margin: "3rem 0", width: "100%",paddingBottom:"3rem" }}>
      <Row
        justify="center"
        gutter={[32, 32]}
        style={{ width: "100%" }}
      >
        <Col
          lg={16}
          md={16}
          sm={24}
          xs={24}
        >
          <Card
            styles={{
              header: {
                background: token.colorPrimaryBg,
              },
              title: {
                fontSize: "1.1em",
              },
              body: {
                textWrap: "wrap",
              },
            }}
            style={{ width: "100%", marginBottom: "3rem" }}
            title={project?.name}
            loading={!project}
          >
            {project && <Markdown components={CodeBlock}>{project.description}</Markdown>}
          </Card>
        </Col>
        <Col
          lg={8}
          md={8}
          sm={24}
          xs={24}
        >
          <Space
            direction="vertical"
            size="large"
            style={{ width: "100%" }}
          >
            <CourseEnrolledView>
              <Tooltip title={now > deadline ? t("project.deadlinePassed") : ""}>
                <span>
                  <SubmissionCard
                    projectId={Number(projectId)}
                    courseId={course.courseId}
                    allowNewSubmission={now < deadline}
                  />
                </span>
              </Tooltip>
            </CourseEnrolledView>

            <GroupCard />

            <CourseEnrolledView>
              <ScoreCard />
            </CourseEnrolledView>
          </Space>
        </Col>
      </Row>

      <Col>
        <CourseAdminView>
          <SubmissionsCard />
        </CourseAdminView>
      </Col>
    </div>
  )
}
export default Project
