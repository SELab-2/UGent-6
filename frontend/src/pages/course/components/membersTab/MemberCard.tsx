import { Card } from "antd"
import { useTranslation } from "react-i18next"
import MembersList from "./MembersList"
import { useState } from "react"
import { CourseMemberType } from "../../../../router/CourseRoutes"

const MembersCard = () => {
  const { t } = useTranslation()
  const [members, setMembers] = useState<CourseMemberType[]>([])


  setTimeout(()=> {
    const you:CourseMemberType = {
      userId: 1,
      relation: "course_admin",
      name: "Wout",
      surname: "wwwwww",
    }
    setMembers([
      {
        userId: 2,
        relation: "enrolled",
        name: "Piet",
        surname: "Jansen",
      },
      {
        userId: 3,
        relation: "enrolled",
        name: "Klaas",
        surname: "Jansen",
      },
      {
        userId: 4,
        relation: "enrolled",
        name: "Jan",
        surname: "Jansen",
      },
      {
        userId: 5,
        relation: "course_admin",
        name: "Bart",
        surname: "Jansen",
      },
      you
    ])
  }, 350)


  return (
    <Card title={t("course.members")}>
      <MembersList members={members} />
    </Card>
  )
}

export default MembersCard
