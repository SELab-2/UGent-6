import { Button, List, Modal, Tooltip } from "antd"
import { FC } from "react"
import { GroupType } from "./GroupList"
import { CloseOutlined } from "@ant-design/icons"
import CourseAdminView from "../../../../hooks/CourseAdminView"
import { useTranslation } from "react-i18next"

const GroupInfoModal: FC<{ group: GroupType | null; open: boolean; setOpen: (b: boolean) => void; removeUserFromGroup: (userId: number,groupId:number) => void }> = ({ group, open, setOpen, removeUserFromGroup }) => {
  
  const { t } = useTranslation()
  if(!group) return null
  return (
    <Modal
      title={group.name}
      open={open}
      onCancel={() => setOpen(false)}
      okButtonProps={{ hidden: true, style: { display: "none" }}}
    >
      <List
        dataSource={group.members ?? []}
        locale={{
          emptyText: t("course.noGroupMembers"),
        }}
        loading={group === null}
        renderItem={(m) => (
          <List.Item
            actions={[
              <CourseAdminView key="remove">
                <Tooltip title={t("group.removeUserFromGroup",{
                  name: m.name
                })}>
                  <Button
                    size="small"
                    type="text"
                    onClick={() => removeUserFromGroup(m.userId,group.groupId!)}
                    icon={<CloseOutlined />}
                  />
                </Tooltip>
              </CourseAdminView>,
            ]}
          >
            {m.name}
          </List.Item>
        )}
      />
    </Modal>
  )
}

export default GroupInfoModal
