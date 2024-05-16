import { useEffect, useState } from "react"
import { useLocation, useNavigate, useParams } from "react-router-dom"
import useApi from "../../hooks/useApi"
import { ApiRoutes } from "../../@types/requests.d"
import { CourseType } from "../course/Course"
import { Button, Card, Spin, Typography, theme } from "antd"
import { useTranslation } from "react-i18next"
import { AppRoutes } from "../../@types/routes"
import CourseTagLine from "../course/components/tabExtraBtn/CourseTagLine"
import useUser from "../../hooks/useUser"
import MarkdownTextfield from "../../components/input/MarkdownTextfield"

const CourseInvite = () => {
  const { token } = theme.useToken()
  const location = useLocation()
  const { courseId } = useParams()
  const API = useApi()
  const [course, setCourse] = useState<CourseType | null>(null)
  const { t } = useTranslation()
  const [loading, setLoading] = useState(false)
  const navigate = useNavigate()
  const {updateCourses} = useUser()

  const getKey = () => {
    const searchParams = new URLSearchParams(location.search)
    return searchParams.get("key")
  }

  useEffect(() => {
    // Get ?key=<key>
    if (!courseId) return
    const key = getKey()
    let ignore = false
    const url = key?.length ? ApiRoutes.COURSE_JOIN : ApiRoutes.COURSE_JOIN_WITHOUT_KEY
    API.GET(
      url,
      { pathValues: { courseId: courseId, courseKey: key || "" } },
      {
        mode: "page",
        errorMessage: t("error.cannotJoinCourse"),
      }
    ).then((res) => {
      if (!ignore && res.success) {
        setCourse(res.response.data)
      }
    })

    return () => {
      ignore = true
    }
  }, [courseId])

  const joinCourse = async () => {
    if (!courseId) return
    setLoading(true)
    const key = getKey()
    const url = key?.length ? ApiRoutes.COURSE_JOIN : ApiRoutes.COURSE_JOIN_WITHOUT_KEY

    API.POST(url, { body: undefined, pathValues: { courseId: courseId, courseKey: key || "" } }, { mode: "message", successMessage: t("course.successfullyRegistered") }).then( async (res) => {
      if (res.success) {
        await updateCourses()
        navigate(AppRoutes.COURSE.replace(":courseId", courseId))
      } else {
        setLoading(false)
      }
    })
  }

  if (!course) {
    return (
      <div style={{ width: "100%", height: "100%", display: "flex", justifyContent: "center", alignItems: "center" }}>
        <Spin size="large" />
      </div>
    )
  }

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
      title={t("course.registerCourse")}
      style={{ margin: "2rem auto", maxWidth: "700px" }}
    >
      <Typography.Title
        level={4}
        style={{ marginTop: 0 }}
      >
        {course.name}
      </Typography.Title>
      <MarkdownTextfield content={course.description} />

      <div style={{ width: "100%", textAlign: "center" }}>
        <Button
          type="primary"
          loading={loading}
          onClick={joinCourse}
        >
          {t("course.register")}
        </Button>
      </div>
    </Card>
  )
}

export default CourseInvite
