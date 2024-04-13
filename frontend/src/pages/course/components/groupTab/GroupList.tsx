import { Button, List, Typography } from "antd"
import { FC, useMemo, useState } from "react"
import { ApiRoutes, GET_Responses } from "../../../../@types/requests"
import useUser from "../../../../hooks/useUser"
import { useTranslation } from "react-i18next"
import GroupInfoModal from "./GroupInfoModal"

export type GroupType = GET_Responses[ApiRoutes.CLUSTER_GROUPS][number]

const Group: FC<{ group: GroupType; canJoin: boolean; canLeave: boolean,onClick:()=>void,onLeave:()=>void, onJoin:()=>void }> = ({ group, canJoin, canLeave,onClick,onJoin,onLeave }) => {
  const { t } = useTranslation()

  return (
    <List.Item
    key={group.groupid}
      actions={[
        <Typography.Text key="cap">
          {group.members.length} / {group.capacity}
        </Typography.Text>,
        canLeave ? (
          <Button size="small" onClick={onLeave} key="leave">{t("course.leaveGroup")}</Button>
        ) : (
          <Button
            key="join"
            size="small"
            disabled={canJoin}
            onClick={onJoin}
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

const GroupList: FC<{ groups: GroupType[] | null }> = ({ groups }) => {
  const { user } = useUser()
  const [modalOpened, setModalOpened] = useState(false)
  const [selectedGroup, setSelectedGroup] = useState<GroupType | null>(null)

  let ownGroupId: number | null = useMemo(() => {
    return groups?.find((group) => group.members.some((u) => u.userid === user?.id))?.groupid ?? null
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
        emptyText: "No groups available",
      }}
      loading={groups === null}
      rowKey="groupid"
      dataSource={groups ?? []}
      renderItem={(g) => (
        <Group
          onClick={()=> handleModalClick(g)}
          canJoin={g.members.length < g.capacity || ownGroupId !== null}
          canLeave={ownGroupId === g.groupid}
          group={g}
          onJoin={() => onJoin(g)}
          onLeave={() => onLeave(g)}
        />
      )}
    />
    
    <GroupInfoModal removeUserFromGroup={removeUserFromGroup} group={selectedGroup} open={modalOpened} setOpen={setModalOpened} />
    </>
  )
}

export default GroupList
