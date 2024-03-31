import { Progress, Tooltip } from "antd"
import { FC } from "react"
import { useTranslation } from "react-i18next";



const GroupProgress:FC<{usersCompleted: number, userCount:number}> = ({ usersCompleted,userCount }) => {
  const {t} = useTranslation()

  return (
    <Tooltip title={t('home.projects.completeProgress', { count: usersCompleted, total: userCount })}>
      <span>
      <Progress percent={Math.floor(usersCompleted / userCount * 100) } />

      </span>
    </Tooltip>
  )
}


export default GroupProgress