import { Card, CardProps, FormInstance } from "antd"
import { FC, PropsWithChildren, useState } from "react"
import { useTranslation } from "react-i18next"
import { TabsProps } from "antd/lib"
import GeneralFormTab from "./projectFormTabs/GeneralFormTab"
import GroupsFormTab from "./projectFormTabs/GroupsFormTab"
import StructureFormTab from "./projectFormTabs/StructureFormTab"
import DockerFormTab from "./projectFormTabs/DockerFormTab"

const VisibleTab: FC<PropsWithChildren<{ visible: boolean }>> = ({ visible, children }) => {
  return <div style={{ display: visible ? "block" : "none" }}>{children}</div>
}

const ProjectForm: FC<PropsWithChildren<{ form: FormInstance, cardProps?: CardProps; activeTab: string; onTabChange: (t: string) => void }>> = ({ children, cardProps, activeTab, onTabChange,form }) => {
  const { t } = useTranslation()

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
      forceRender: true,
    },
  ]

  // Note: we need to render all tabs, even if they are not visible. Otherwise the form cannot get its values
  return (
    <Card
      {...cardProps}
      style={{ maxWidth: "700px", width: "100%", margin: "2rem 0" }}
      tabList={tabs}
      tabProps={{
        size: "middle",
        activeKey: activeTab,
      }}
      onTabChange={onTabChange}
    >
      <VisibleTab visible={activeTab === "general"}>
        <GeneralFormTab />
      </VisibleTab>
      <VisibleTab visible={activeTab === "groups"}>
        <GroupsFormTab />
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
