import { Card, CardProps, FormInstance } from "antd"
import { FC, PropsWithChildren, useState } from "react"
import { useTranslation } from "react-i18next"
import { TabsProps } from "antd/lib"
import GeneralFormTab from "./projectFormTabs/GeneralFormTab"
import GroupsFormTab from "./projectFormTabs/GroupsFormTab"
import StructureFormTab from "./projectFormTabs/StructureFormTab"
import DockerFormTab from "./projectFormTabs/DockerFormTab"
import { useLocation, useNavigate } from "react-router-dom"

const VisibleTab: FC<PropsWithChildren<{ visible: boolean }>> = ({ visible, children }) => {
  return <div style={{ display: visible ? "block" : "none" }}>{children}</div>
}

const ProjectForm: FC<PropsWithChildren<{ form: FormInstance, cardProps?: CardProps; }>> = ({ children, cardProps, form }) => {
  const { t } = useTranslation()
  const location = useLocation()
  const navigate = useNavigate()

  const tabs: TabsProps["items"] = [
    {
      key: "general",
      label: t("project.change.general"),
    },
    {
      key: "groups",
      label: t("project.change.groups"),
    },
    {
      key: "structure",
      label: t("project.change.structure"),
    },
    {
      key: "tests",
      label: t("project.change.tests"),
    },
  ]


  const onTabChange = (key: string) => {
    navigate(`#${key}`)
  }

  const activeTab = location.hash.slice(1) || "general"

  // Note: we need to render all tabs, even if they are not visible. Otherwise the form cannot get its values
  return (
    <Card
      {...cardProps}
      style={{ maxWidth: "700px", width: "100%", margin: "2rem 0" }}
      tabList={tabs}
      tabProps={{
        size: "middle",
        activeKey: activeTab,
        defaultActiveKey:location.hash.slice(1) || "general"

      }}
      onTabChange={onTabChange}
    >
      <VisibleTab visible={activeTab === "general"}>
        <GeneralFormTab />
      </VisibleTab>
      <VisibleTab visible={activeTab === "groups"}>
        <GroupsFormTab form={form} />
      </VisibleTab>
      <VisibleTab visible={activeTab === "structure"}>
        <StructureFormTab form={form}/>
      </VisibleTab>
      <VisibleTab visible={activeTab === "tests"}>
        <DockerFormTab form={form}/>
      </VisibleTab>

      {children}
    </Card>
  )
}

export default ProjectForm
