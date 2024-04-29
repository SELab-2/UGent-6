import { FC, useEffect, useMemo, useState } from "react"
import { GroupType } from "../../pages/project/components/GroupTab"

import { Alert, Button, Select, Space, Switch, Table, Transfer } from "antd"
import type { GetProp, SelectProps, TableColumnsType, TableProps, TransferProps } from "antd"
import apiCall from "../../util/apiFetch"
import { ApiRoutes } from "../../@types/requests.d"
import { CourseMemberType } from "../../pages/course/components/membersTab/MemberCard"
import { useTranslation } from "react-i18next"

type TransferItem = GetProp<TransferProps, "dataSource">[number]
type TableRowSelection<T extends object> = TableProps<T>["rowSelection"]

interface TableTransferProps extends TransferProps<TransferItem> {
  dataSource: CourseMemberType[]
  leftColumns: TableColumnsType<CourseMemberType>
  rightColumns: TableColumnsType<CourseMemberType>
}

// Customize Table Transfer
const TableTransfer = ({ leftColumns, rightColumns, emptyText, ...restProps }: TableTransferProps & { emptyText: string, warning?:string }) => (
  <Transfer {...restProps}>
    {({ direction, filteredItems, onItemSelect, onItemSelectAll, selectedKeys: listSelectedKeys, disabled: listDisabled }) => {
      const columns = direction === "left" ? leftColumns : rightColumns

      const rowSelection: TableRowSelection<TransferItem> = {
        getCheckboxProps: () => ({ disabled: listDisabled }),
        onChange(selectedRowKeys) {
          onItemSelectAll(selectedRowKeys as string[], "replace")
        },
        selectedRowKeys: listSelectedKeys,
        selections: [Table.SELECTION_ALL, Table.SELECTION_INVERT, Table.SELECTION_NONE],
      }

      return (<>
      {restProps.warning && <Alert style={{margin:"1rem 0.5rem"}} type="warning" message={restProps.warning} />}
        <Table
          locale={{
            emptyText,
          }}
          rowSelection={rowSelection}
          columns={columns}
          dataSource={filteredItems}
          size="small"
          style={{ pointerEvents: listDisabled ? "none" : undefined }}
          onRow={({ key, disabled: itemDisabled }) => ({
            onClick: () => {
              if (itemDisabled || listDisabled) {
                return
              }
              onItemSelect(key, !listSelectedKeys.includes(key))
            },
          })}
        />
   </>   )
    }}
  </Transfer>
)

const GroupMembersTransfer: FC<{ groups: GroupType[]; onChanged: () => void; courseId: number | string }> = ({ groups, onChanged, courseId }) => {
  const [targetKeys, setTargetKeys] = useState<Record<string, TransferProps["targetKeys"]>>({})
  const [courseMembers, setCourseMembers] = useState<CourseMemberType[] | null>(null)
  const [selectedGroup, setSelectedGroup] = useState<GroupType | null>(null)
  const { t } = useTranslation()


  useEffect(()=> {
    if(courseMembers === null || !groups?.length) return
    setSelectedGroup(groups[0])
  },[groups, courseMembers])


  useEffect(() => {
    fetchCourseMembers()
  }, [courseId])

  const fetchCourseMembers = async () => {
    const response = await apiCall.get(ApiRoutes.COURSE_MEMBERS, { courseId })
    setCourseMembers(response.data)
  }

  const onChange: TableTransferProps["onChange"] = (nextTargetKeys) => {
    if (!selectedGroup) return console.error("No group selected")
    setTargetKeys((curr) => ({ ...curr, [selectedGroup?.groupId]: nextTargetKeys }))
    // TODO: make api call
  }

  const columns: TableColumnsType<CourseMemberType> = [
    {
      key: "title",
      title: t("project.change.name"),
      render: (courseMember: CourseMemberType) => courseMember.user.name,
    },
    {
      key: "email",
      render: (courseMember: CourseMemberType) => courseMember.user.email,

      title: "Email",
    },
  ]

  const changeGroup: SelectProps["onChange"] = (e: number) => {
    const group = groups.find((g) => g.groupId === e)
    if (group == null) return console.error("Group not found: " + e)
    setSelectedGroup(group)
  }


  const randomizeGroups = () => {
      if(!courseMembers) {
        return
      }
      let randomGroups: Record<string, string[]> = {}

      let members = [...courseMembers]
      members = members.sort(() => Math.random() - 0.5)
      for (let i = 0; i < groups.length; i++) {
        const group = groups[i]
        const groupMembers = members.splice(0, group.capacity)
        // @ts-ignore //TODO: fix the types so i can remove the ts ignore
        randomGroups[group.groupId] = groupMembers.map((m) => m.user.userId)
      }
      console.log(randomGroups);
      setTargetKeys(randomGroups)
  }
console.log("---->",selectedGroup?.groupId);
  const renderFooter: TransferProps["footer"] = (_, info) => {
    // Filter `option.label` match the user type `input`
    const filterOption = (input: string, option?: { label: string }) => (option?.label ?? "").toLowerCase().includes(input.toLowerCase())

    return (
      <div style={{ margin: "0.5rem", textAlign: "center" }}>
        {
          info?.direction === "left" ? <Button disabled={!courseMembers} onClick={randomizeGroups}>{t("project.change.randomizeGroups")}</Button>:
          <Select
            showSearch
            value={selectedGroup?.groupId}
            placeholder={t("project.change.selectGroup")}
            optionFilterProp="children"
            onChange={changeGroup}
            filterOption={filterOption}
            style={{width:"100%"}}
            options={groups.map((g) => ({ label: g.name, value: g.groupId }))}
          />
        }



      </div>
    )
  }


  // Filter out the users that are already in a group
  const dataSource = useMemo(()=> {
    if(!selectedGroup || !courseMembers) return []
    let users = new Set<string>()

    const selectedGroupId = selectedGroup.groupId.toString()
    for(const groupId in targetKeys) {
      if(groupId === selectedGroupId) continue

      targetKeys[groupId]?.forEach((key) => users.add(key.toString()))
    }

    return courseMembers.filter((u) => !users.has(u.user.userId.toString()))
  },[selectedGroup,courseMembers])

  const overCapacity = selectedGroup && (targetKeys[selectedGroup.groupId]?.length??0) >  selectedGroup.capacity

  return (
    <>
      <TableTransfer
        locale={{
          searchPlaceholder: t("project.change.searchUser"),
        }}
        emptyText={t("project.change.noMembersinGroup")}
        dataSource={dataSource}
        targetKeys={selectedGroup?.groupId ? targetKeys[selectedGroup?.groupId] : []}
        showSearch
        rowKey={(r) => r.user.userId}
        showSelectAll={false}
        onChange={onChange}
        filterOption={(inputValue, item) => item.user.name!.indexOf(inputValue) !== -1}
        leftColumns={columns}
        rightColumns={columns}
        warning={overCapacity ? t("project.change.groupFull") : undefined}
        footer={renderFooter}
        status={overCapacity ? "warning": undefined}
      />
    </>
  )
}

export default GroupMembersTransfer
