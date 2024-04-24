import { Button, Card, CardProps, Checkbox, DatePicker, Form, FormInstance, Input, Switch } from "antd"
import { FC, PropsWithChildren, useState } from "react"
import { useTranslation } from "react-i18next"
import { useParams } from "react-router-dom"
import { TabsProps } from "antd/lib"
import GeneralFormTab from "./projectFormTabs/GeneralFormTab"
import GroupsFormTab from "./projectFormTabs/GroupsFormTab"


const projectForms:Record<string, FC> = {
  general: GeneralFormTab,
  groups: GroupsFormTab,
  structure: ()=> null,
  tests: () =>null
}

const ProjectForm: FC<PropsWithChildren<{cardProps?: CardProps}>> = ({ children,cardProps }) => {
  const { t } = useTranslation()
  const [tab,setTab] = useState<string>("general")

  const tabs:TabsProps["items"] = [
    {
      key: "general",
      label: t("project.change.general"),
      forceRender: true
    },
    {
      key: "groups",
      label: t("project.change.groups"),
      forceRender: true
    },
    {
      key: "structure",
      label: t("project.change.structure"),
      forceRender: true
    },
    {
      key: "tests",
      label: t("project.change.tests"),
      forceRender: true
    }
  ]
  const ActiveForm = projectForms[tab];

  return (
    <Card
    {...cardProps}
      style={{ maxWidth: "700px", width: "100%", margin: "2rem 0" }}
      tabList={tabs}
      tabProps={{
        size: 'middle',
        
      }}
      
      onTabChange={setTab}
    >
        <ActiveForm />
      
      {children}
    </Card>
  )
}

export default ProjectForm
