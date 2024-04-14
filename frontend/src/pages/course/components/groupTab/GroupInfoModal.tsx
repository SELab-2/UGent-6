import { Button, List, Modal, Tooltip } from "antd"
import { FC } from "react"
import { GroupType } from "./GroupList"
import { CloseOutlined } from "@ant-design/icons"
import CourseAdminView from "../../../../hooks/CourseAdminView"
import { useTranslation } from "react-i18next"

const GroupInfoModal: FC<{ group: GroupType | null; open: boolean; setOpen: (b: boolean) => void; removeUserFromGroup: (userId: number) => void }> = ({ group, open, setOpen, removeUserFromGroup }) => {
  
  const { t } = useTranslation()

  return (
    <Modal
      title={group?.name}
      open={open}
      onCancel={() => setOpen(false)}
    >
      <List
        dataSource={group?.members ?? []}
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
                    onClick={() => removeUserFromGroup(m.userid)}
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
