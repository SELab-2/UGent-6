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

const Group: FC<{ group: GroupType; canJoin: boolean; canLeave: boolean; onClick: () => void; onLeave: () => void; onJoin: () => void; loading?: boolean }> = ({ group, canJoin, canLeave, onClick, onJoin, onLeave, loading }) => {
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
            disabled={!canJoin}
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

const GroupList: FC<{ groups: GroupType[] | null; project?: number | ProjectType | null; onChanged?: () => Promise<void> }> = ({ groups, project, onChanged }) => {
  const [modalOpened, setModalOpened] = useState(false)
  const [selectedGroup, setSelectedGroup] = useState<number | null>(null)
  const [groupId, setGroupId] = useState<number | null>(null)
  const [loading, setLoading] = useState(false)
  const { t } = useTranslation()
  const { message } = useAppApi()
  const { user } = useUser()
  const { courseId } = useParams<{ courseId: string }>()

  useEffect(() => {
    if (typeof project === "number") return setGroupId(project)
    if (project !== undefined) return setGroupId(project?.groupId ?? null)
    if (!courseId) return

    let ignore = false

    const fetchOwnGroup = async () => {
      if (!user) return
      try {
        const res = await apiCall.get(ApiRoutes.PROJECT, { id: courseId })
        if (!ignore) setGroupId(res.data.groupId ?? null)

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
    setSelectedGroup(group.groupId)
    setModalOpened(true)
  }

  const removeUserFromGroup = async (userId: number, groupId: number) => {
    try {
      setLoading(true)
      await apiCall.delete(ApiRoutes.GROUP_MEMBER, undefined, { id: groupId, userId: userId })
      if(onChanged) await onChanged()

      setGroupId(null)
      message.success(t("course.leftGroup"))
    } catch (err) {
      console.error(err)
      // TODO: handle error
    } finally {
      setLoading(false)
    }
  }

  const onLeave = async (group: GroupType) => {
    if (!user) return
    removeUserFromGroup(user.id, group.groupId)
  }

  const onJoin = async (group: GroupType) => {
    // TODO: join group request
    if (!user) return
    try {
      setLoading(true)
      await apiCall.post(ApiRoutes.GROUP_MEMBERS, { id: user.id }, { id: group.groupId })
      if(onChanged) await onChanged()

      message.success(t("course.joinedGroup"))
      setGroupId(group.groupId)
    } catch (err) {
      console.error(err)
    }
    setLoading(false)
  }


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
        group={selectedGroup && groups ? groups.find((g) => g.groupId === selectedGroup) ?? null : null}
        open={modalOpened}
        setOpen={setModalOpened}
      />
    </>
  )
}

export default GroupList
