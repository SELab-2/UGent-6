import { Card, Input } from "antd"
import { useTranslation } from "react-i18next"
import MembersList from "./MembersList"
import { useEffect, useMemo, useState } from "react"
import apiCall from "../../../../util/apiFetch"
import { ApiRoutes, GET_Responses } from "../../../../@types/requests.d"
import useCourse from "../../../../hooks/useCourse"

export type CourseMemberType = GET_Responses[ApiRoutes.COURSE_MEMBERS][number]

const MembersCard = () => {
  const { t } = useTranslation()
  const course = useCourse()
  const [members, setMembers] = useState<CourseMemberType[] | null>(null)
  const [search, setSearch] = useState<string>("")
  
  useEffect(() => {
    if (!course) return console.error("No courseId found")

    let ignore = false
    apiCall.get(course.memberUrl).then((res) => {
      console.log(res.data)
      setMembers(res.data)
    })
    return () => {
      ignore = true
    }
  }, [course])

  const filteredMembers = useMemo(() => {
    if (!members) return null
    if (!search) return members

    return members.filter((member) => {
      return member.user.name.toLowerCase().includes(search.toLowerCase())
    })
  }, [members, search])

  return (
    <>
      <Card
        title={t("course.members")}
        extra={
          <Input.Search
            onChange={(e) => setSearch(e.target.value)}
            placeholder={t("course.searchMember")}
          />
        }
      >
        <MembersList members={filteredMembers} />
      </Card>
      <br />
      <br />
    </>
  )
}

export default MembersCard
