import { Button, List, Typography } from "antd"
import { FC, useMemo, useState } from "react"
import { ApiRoutes, GET_Responses } from "../../../../@types/requests.d"
import useUser from "../../../../hooks/useUser"
import { useTranslation } from "react-i18next"
import GroupInfoModal from "./GroupInfoModal"

export type GroupType = GET_Responses[ApiRoutes.GROUP]

const Group: FC<{ group: GroupType;capacity:number, canJoin: boolean; canLeave: boolean,onClick:()=>void,onLeave:()=>void, onJoin:()=>void }> = ({ group, canJoin, canLeave,onClick,onJoin,onLeave,capacity }) => {
  const { t } = useTranslation()
  return (
    <List.Item
    key={group.groupid}
      actions={[
        <Typography.Text key="cap">
          {group.members.length} / {capacity}
        </Typography.Text>,
        canLeave ? (
          <Button  style={{width:"130px"}} size="small" onClick={onLeave} key="leave">{t("course.leaveGroup")}</Button>
        ) : (
          <Button
            key="join"
            size="small"
            disabled={canJoin}
            onClick={onJoin}
            style={{width:"130px"}}
          >
            {t("course.joinGroup")}
          </Button>
        ),
      ]}
    >
      <List.Item.Meta title={<Button size="small" onClick={onClick} type="link">{group.name}</Button>} />
    </List.Item>
  )
}

const GroupList: FC<{ groups: GroupType[] | null, capacity:number }> = ({ groups,capacity }) => {
  const { user } = useUser()
  const [modalOpened, setModalOpened] = useState(false)
  const [selectedGroup, setSelectedGroup] = useState<GroupType | null>(null)
  const {t} = useTranslation()

  // TODO: fix this
  let ownGroupId: number | null = useMemo(() => {
    return 1 // groups?.find((group) => group.members.some((u) => u.userId === user?.id))?.groupId ?? null
  }, [groups])


  const handleModalClick = (group:GroupType) => {
    setSelectedGroup(group)
    setModalOpened(true)
  }

  const onLeave = (group:GroupType) => {
    // TODO: leave group request
  }

  const onJoin = (group:GroupType) => {
    // TODO: join group request
  }

  const removeUserFromGroup = (userId: number) => {
      // TODO: remove user fom group request

  }


  return (<>
    <List
      locale={{
        emptyText: t("course.noGroups") ,
      }}
      loading={groups === null}
      rowKey="groupid"
      dataSource={groups ?? []}
      renderItem={(g) => (
        <Group
          onClick={()=> handleModalClick(g)}
          canJoin={g.members.length < capacity || ownGroupId !== null}
          canLeave={ownGroupId === g.groupid}
          group={g}
          onJoin={() => onJoin(g)}
          onLeave={() => onLeave(g)}
          capacity={capacity}
        />
      )}
    />
    
    <GroupInfoModal removeUserFromGroup={removeUserFromGroup} group={selectedGroup} open={modalOpened} setOpen={setModalOpened} />
    </>
  )
}

export default GroupList
