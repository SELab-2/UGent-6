import { FC, useEffect, useState } from "react"
import GroupList, { GroupType } from "./GroupList"

// Atm not used! TODO: remove this if not needed anymore in future
const GroupCollapseItem: FC<{ clustedId: number }> = ({ clustedId }) => {
  const [group, setGroup] = useState<GroupType[] | null>(null)

  useEffect(() => {
    // TODO: make request to `groupUrl`

    
  }, [])



  return <GroupList groups={group} capacity={1} />
}

export default GroupCollapseItem
