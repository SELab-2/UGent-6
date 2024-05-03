import { Badge, BadgeProps, Calendar } from "antd";
import { FC } from "react";
import type { Dayjs } from 'dayjs';
import dayjs from 'dayjs';
import { ProjectType } from "../../pages/project/Project";
import { ProjectStatus } from "../../@types/requests";
import { Link } from "react-router-dom";
import { AppRoutes } from "../../@types/routes";



const projectStatusToBadge:Record<ProjectStatus, BadgeProps['status']> = {
  "not started": "default",
  correct: "success",
  incorrect: "error",
}

const ProjectCalander:FC< {projects: ProjectType[]|null}> = ({projects}) => {


  const getListData = (value: Dayjs) => {
    if (!projects) return [];

    return projects.filter(project => 
      dayjs(project.deadline).isSame(value, 'day')
    );
  }
  
  const dateCellRender = (value: Dayjs) => {
    const listData = getListData(value);
    return (
      <ul className="events">
        {listData.map((p) => (
          <li key={p.projectId}>
            <Badge status={projectStatusToBadge[p.status??"not started"]} text={<Link to={AppRoutes.PROJECT.replace(":courseId", p.course.courseId + "").replace(":projectId", p.projectId + "")} >{p.name}</Link> } />
          </li>
        ))}
      </ul>
    );
  };

  return   <Calendar cellRender={dateCellRender} />

  
}


export default ProjectCalander