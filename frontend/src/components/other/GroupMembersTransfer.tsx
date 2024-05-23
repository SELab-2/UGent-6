import { FC, useEffect, useMemo, useState } from "react"
import { GroupType } from "../../pages/project/components/GroupTab"

import { Alert, Button, Select, Table, Transfer } from "antd"
import type { GetProp, SelectProps, TableColumnsType, TableProps, TransferProps } from "antd"
import { ApiRoutes } from "../../@types/requests.d"
import { CourseMemberType } from "../../pages/course/components/membersTab/MemberCard"
import { useTranslation } from "react-i18next"
import useApi from "../../hooks/useApi"

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

export type GroupMembers = Record<string, number[]>

const GroupMembersTransfer: FC<{ value?: GroupMembers,groups: GroupType[]; onChange?: (newTargetKeys:GroupMembers) => void; courseId: number | string }> = ({ groups, onChange, courseId, value, ...args }) => {
  const [courseMembers, setCourseMembers] = useState<CourseMemberType[] | null>(null)
  const [selectedGroup, setSelectedGroup] = useState<GroupType | null>(null)
  const { t } = useTranslation()
  const API = useApi()

  useEffect(()=> {
    if(courseMembers === null || !groups?.length) return


    let groupsMembers:GroupMembers = {}
    for( let group of groups) {
      groupsMembers[group.name] = group.members.map((m) => m.userId)
    }
    if(onChange) onChange(groupsMembers)

    setSelectedGroup(groups[0])


  },[groups, courseMembers])


  useEffect(() => {
    fetchCourseMembers()
  }, [courseId])

  const fetchCourseMembers = async () => {
    const response = await API.GET(ApiRoutes.COURSE_MEMBERS, { pathValues: { courseId } },"message")
    if(!response.success) return

    setCourseMembers(response.response.data.filter(m => m.relation === "enrolled"))
  }

  const onChangeHandler: TableTransferProps["onChange"] = (nextTargetKeys) => {
    if (!selectedGroup) return console.error("No group selected")
    const newTargetKeys = { ...value, [selectedGroup?.name]: nextTargetKeys as any as number[]}
    // setTargetKeys(newTargetKeys)
    if(onChange) onChange(newTargetKeys)
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

  const changeGroup: SelectProps["onChange"] = (e: string) => {
    const group = groups.find((g) => g.name === e)
    if (group == null) return console.error("Group not found: " + e)
    setSelectedGroup(group)
  }


  const randomizeGroups = () => {
      if(!courseMembers) {
        return
      }
      let randomGroups: Record<string, number[]> = {}

      let members = [...courseMembers]
      members = members.sort(() => Math.random() - 0.5)
      for (let i = 0; i < groups.length; i++) {
        const group = groups[i]
        const groupMembers = members.splice(0, group.capacity)
        randomGroups[group.name] = groupMembers.map((m) => m.user.userId)
      }
      // setTargetKeys(randomGroups)
      if(onChange) onChange(randomGroups)
  }

  const renderFooter: TransferProps["footer"] = (_, info) => {
    // Filter `option.label` match the user type `input`
    const filterOption = (input: string, option?: { label: string }) => (option?.label ?? "").toLowerCase().includes(input.toLowerCase())

    return (
      <div style={{ margin: "0.5rem", textAlign: "center" }}>
        {
          info?.direction === "left" ? <Button disabled={!courseMembers} onClick={randomizeGroups}>{t("project.change.randomizeGroups")}</Button>:
          <Select
            showSearch
            value={selectedGroup?.name}
            placeholder={t("project.change.selectGroup")}
            optionFilterProp="children"
            onChange={changeGroup}
            filterOption={filterOption}
            style={{width:"100%"}}
            options={groups.map((g) => ({ label: g.name, value: g.name }))}
          />
        }
      </div>
    )
  }


  // Filter out the users that are already in a group
  const dataSource = useMemo(()=> {
    if(!selectedGroup || !courseMembers) return []
    let users = new Set<string>()

    const selectedname = selectedGroup.name.toString()
    for(const name in value) {
      if(name === selectedname) continue
      value[name]?.forEach((key) => users.add(key.toString()))
    }

    return courseMembers.filter((u) => !users.has(u.user.userId.toString()))
  },[selectedGroup,courseMembers,groups,value])
  const overCapacity = selectedGroup && value && (value[selectedGroup.name]?.length??0) >  selectedGroup.capacity

  return (
    <>
      <TableTransfer
        {...args}
        locale={{
          searchPlaceholder: t("project.change.searchUser"),
        }}
        emptyText={t("project.change.noMembersinGroup")}
        dataSource={dataSource}
        targetKeys={selectedGroup?.name && value ? value[selectedGroup?.name] as any as string[]: []}
        showSearch
        rowKey={(r) => r.user.userId}
        showSelectAll={false}
        onChange={onChangeHandler}
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
