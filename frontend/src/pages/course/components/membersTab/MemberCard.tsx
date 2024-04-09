import { Card } from "antd"
import { useTranslation } from "react-i18next"
import MembersList from "./MembersList"
import { useEffect, useState } from "react"
import apiCall from "../../../../util/apiFetch"
import { ApiRoutes, GET_Responses } from "../../../../@types/requests.d"
import useCourse from "../../../../hooks/useCourse"

export  type CourseMemberType = GET_Responses[ApiRoutes.COURSE_MEMBERS][number]

const MembersCard = () => {
  const { t } = useTranslation()
  const course = useCourse()
  const [members, setMembers] = useState<CourseMemberType[] | null>(null)

  useEffect(()=> {

    if(!course) return console.error("No courseId found")

    let ignore = false
    apiCall.get(course.memberUrl).then((res) => {
      console.log(res.data)
      setMembers(res.data)
    })
    return () => {
      ignore = true
    }
  },[course])


   

    return (
    <>
      <Card title={t("course.members")}>
        <MembersList members={members} />
      </Card>
      <br />
      <br />
    </>
  )
}

export default MembersCard
