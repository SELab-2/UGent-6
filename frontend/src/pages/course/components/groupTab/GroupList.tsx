import { Button, List, Typography } from "antd"
import { FC, useEffect, useMemo, useState } from "react"
import { ApiRoutes, GET_Responses } from "../../../../@types/requests.d"
import useUser from "../../../../hooks/useUser"
import { useTranslation } from "react-i18next"
import GroupInfoModal from "./GroupInfoModal"
import useAppApi from "../../../../hooks/useAppApi"
import apiCall from "../../../../util/apiFetch"
import { ProjectType } from "../../../project/Project"
import { useParams } from "react-router-dom"

export type GroupType = GET_Responses[ApiRoutes.GROUP]

const Group: FC<{ group: GroupType; canJoin: boolean; canLeave: boolean; onClick: () => void; onLeave: () => void; onJoin: () => void,loading?:boolean }> = ({ group, canJoin, canLeave, onClick, onJoin, onLeave,loading }) => {
  const { t } = useTranslation()

  return (
    <List.Item
      key={group.groupId}
      actions={[
        <Typography.Text key="cap">
          {group.members.length} / {group.capacity}
        </Typography.Text>,
        canLeave ? (
          <Button
            style={{ width: "130px" }}
            size="small"
            loading={loading}
            onClick={onLeave}
            key="leave"
          >
            {t("course.leaveGroup")}
          </Button>
        ) : (
          <Button
            key="join"
            loading={loading}
            size="small"
            disabled={canJoin}
            onClick={onJoin}
            style={{ width: "130px" }}
          >
            {t("course.joinGroup")}
          </Button>
        ),
      ]}
    >
      <List.Item.Meta
        title={
          <Button
            size="small"
            onClick={onClick}
            type="link"
          >
            {group.name || "Groep " + group.members.map((m) => m.name).slice(25)}
          </Button>
        }
      />
    </List.Item>
  )
}

const GroupList: FC<{ groups: GroupType[] | null; project?: ProjectType | null }> = ({ groups, project }) => {
  const [modalOpened, setModalOpened] = useState(false)
  const [selectedGroup, setSelectedGroup] = useState<GroupType | null>(null)
  const [groupId, setGroupId] = useState<number | null>(null)
  const [loading, setLoading] = useState(false)
  const { t } = useTranslation()
  const { message } = useAppApi()
  const { user } = useUser()
  const { courseId } = useParams<{ courseId: string }>()

  useEffect(() => {
    if (project !== undefined) return setGroupId(project?.groupId ?? null)
    if (!courseId) return

    let ignore = false

    const fetchOwnGroup = async () => {
      if (!user) return
      try {
        const res = await apiCall.get(ApiRoutes.GROUP, { id: courseId })
        if (!ignore) setGroupId(res.data.groupId ?? null)

          console.log(res.data);
      } catch (err) {
        console.error(err)
      }
    }
    fetchOwnGroup()
    return () => {
      ignore = true
    }
  }, [groups, project, courseId])

  const handleModalClick = (group: GroupType) => {
    setSelectedGroup(group)
    setModalOpened(true)
  }

  const onLeave = async (group: GroupType) => {
    if (!user || !groupId) return
    try {
      setLoading(true)
      await apiCall.delete(ApiRoutes.GROUP_MEMBER, undefined, { id: groupId, userId: user.id })
      console.log(group)
      setGroupId(null)
      message.success(t("course.leftGroup"))
    } catch (err) {
      console.error(err)
      // TODO: handle error
    } finally {
      setLoading(false)
    }
  }

  const onJoin = async (group: GroupType) => {
    // TODO: join group request
    if (!user) return
    try {
      setLoading(true)
      await apiCall.post(ApiRoutes.GROUP_MEMBERS, { id: user.id }, { groupId: group.groupId })
      message.success(t("course.joinedGroup"))
      setGroupId(group.groupId)
    } catch (err) {
      console.error(err)
    }
  }

  const removeUserFromGroup = (userId: number) => {
    // TODO: remove user fom group request
  }



  console.log(groupId);
  return (
    <>
      <List
        locale={{
          emptyText: t("course.noGroups"),
        }}
        loading={groups === null}
        rowKey="groupId"
        dataSource={groups ?? []}
        renderItem={(g) => (
          <Group
            onClick={() => handleModalClick(g)}
            canJoin={g.members.length < g.capacity || groupId !== null}
            canLeave={groupId === g.groupId}
            group={g}
            loading={loading}
            onJoin={() => onJoin(g)}
            onLeave={() => onLeave(g)}
          />
        )}
      />

      <GroupInfoModal
        removeUserFromGroup={removeUserFromGroup}
        group={selectedGroup}
        open={modalOpened}
        setOpen={setModalOpened}
      />
    </>
  )
}

export default GroupList
