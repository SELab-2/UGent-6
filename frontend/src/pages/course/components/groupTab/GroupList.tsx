import { Button, Input, List, Typography } from "antd"
import { FC, useEffect, useMemo, useState } from "react"
import { ApiRoutes, GET_Responses } from "../../../../@types/requests.d"
import useUser from "../../../../hooks/useUser"
import { useTranslation } from "react-i18next"
import GroupInfoModal from "./GroupInfoModal"
import useAppApi from "../../../../hooks/useAppApi"
import { ProjectType } from "../../../project/Project"
import { useParams } from "react-router-dom"
import useApi from "../../../../hooks/useApi"
import useIsCourseAdmin from "../../../../hooks/useIsCourseAdmin"
import { ClusterType } from "./GroupsCard"
import { PlusOutlined } from "@ant-design/icons"
import CourseAdminView from "../../../../hooks/CourseAdminView"

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
            loading={loading && canJoin}
            size="small"
            disabled={!canJoin}
            onClick={onJoin}
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

const GroupList: FC<{ locked: ClusterType["lockGroupsAfter"]; groups: GroupType[] | null; project?: number | ProjectType | null; onChanged?: () => Promise<void>; onGroupIdChange?: (groupId: number | null) => void, clusterId: number|null }> = ({ groups, project, onChanged, onGroupIdChange, locked,clusterId }) => {
  const [modalOpened, setModalOpened] = useState(false)
  const [selectedGroup, setSelectedGroup] = useState<number | null>(null)
  const [groupId, setGroupId] = useState<number | null>(null)
  const [loading, setLoading] = useState(false)
  const [createLoading, setCreateLoading] = useState(false)
  const { t } = useTranslation()
  const { message } = useAppApi()
  const { user } = useUser()
  const { courseId } = useParams<{ courseId: string }>()
  const isCourseAdmin = useIsCourseAdmin()
  const API = useApi()

  const isLocked = useMemo(() => {
    if (!locked) return false
    return new Date(locked).getTime() < Date.now()
  }, [locked])

  useEffect(() => {
    if (typeof project === "number") return setGroupId(project)
    if (project !== undefined) return setGroupId(project?.groupId ?? null)
    if (!courseId) return

    let ignore = false
    return () => {
      ignore = true
    }
  }, [project, courseId])

  const handleModalClick = (group: GroupType) => {
    setSelectedGroup(group.groupId)
    setModalOpened(true)
  }

  const removeUserFromGroup = async (userId: number, groupId: number) => {
    setLoading(true)
    const response = await API.DELETE(ApiRoutes.GROUP_MEMBER, { pathValues: { id: groupId, userId: userId } }, "message")
    if (!response.success) return setLoading(false)

    setGroupId(null)
    if (onChanged) await onChanged()
    if (onGroupIdChange) onGroupIdChange(null)

    message.success(t("course.leftGroup"))

    setLoading(false)
  }

  const onLeave = async (group: GroupType) => {
    if (!user) return
    removeUserFromGroup(user.id, group.groupId)
  }

  const onJoin = async (group: GroupType) => {
    if (!user) return
    setLoading(true)
    const response = await API.POST(ApiRoutes.GROUP_MEMBERS, { body: { id: user.id }, pathValues: { id: group.groupId } }, "message")
    if (!response.success) return setLoading(false)
    if (onChanged) await onChanged()

    message.success(t("course.joinedGroup"))
    setGroupId(group.groupId)
    if (onGroupIdChange) onGroupIdChange(group.groupId)
    setLoading(false)
  }

  const addCreateGroupModal = async () => {
    if(!clusterId || groups===null) return
    setCreateLoading(true)
    const r = await API.POST(ApiRoutes.CLUSTER_GROUPS, { body: { name: "Group " + (groups.length+1)}, pathValues: { id: clusterId } }, "message")
    setCreateLoading(false)
    if(!r.success) return

    if (onChanged) await onChanged()
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
            canJoin={g.members.length < g.capacity && groupId === null && !isCourseAdmin && !isLocked}
            canLeave={groupId === g.groupId && !isLocked}
            group={g}
            loading={loading}
            onJoin={() => onJoin(g)}
            onLeave={() => onLeave(g)}
          />
        )}
      />
      <CourseAdminView>
      {clusterId && <div style={{ textAlign: "center" }}>
          <Button
            type="text"
            icon={<PlusOutlined />}
            loading={createLoading}
            onClick={addCreateGroupModal}
          >
            {t("course.addGroup")}
          </Button>
        </div>}
      </CourseAdminView>

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
