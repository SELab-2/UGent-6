import CourseCard from '../../src/pages/index/components/CourseCard'
import {BrowserRouter} from "react-router-dom";
import {ApiRoutes, CourseRelation, DockerFeedback, DockerStatus, Timestamp} from "../../src/@types/requests";

const mockProjects = [
    {course: {name: "test course 1", url: "test course 1 url", courseId: 1908},
      deadline: "NOW",
      description: "do something",
      clusterId: null,
      projectId: 35,
      name: "test project 1",
      submissionUrl: null,
      testsUrl: "test project 1 url",
      maxScore: null,
      visible: true,
      progress: {
        completed: 5,
        total: 10,
      },
      groupId: null,
    },
  {course: {name: "test course 1", url: "test course 1 url", courseId: 1908},
    deadline: "NOW",
    description: "do something",
    clusterId: null,
    projectId: 36,
    name: "test project 2",
    submissionUrl: null,
    testsUrl: "test project 2 url",
    maxScore: null,
    visible: true,
    progress: {
      completed: 0,
      total: 10,
    },
    groupId: null,
  }
  ]


const mockCourse = {
  courseId: 1908,
  name: "test course 1",
  relation: "enrolled" as CourseRelation,
  memberCount: 20,
  archivedAt: null,
  year: 2023,
  url: "test course 1 url"
}

Cypress.on('uncaught:exception', (err: any, runnable: any) => {
  return false
})

describe('<CourseCard />', () => {
  it('renders', () => {
    // see: https://on.cypress.io/mounting-react
    cy.mount(<BrowserRouter><CourseCard adminView={false} courseProjects={{projects: mockProjects, course: mockCourse }} /></BrowserRouter>).should("exist")
    cy.get('.ant-card-head').should("contain.text", "test course 1")
    cy.get('.ant-list-items > :nth-child(1)').should("contain.text", "test project 1")
        .and("contain.text", "50%")
    cy.get('.ant-list-items > :nth-child(2)').should("contain.text", "test project 2")
        .and("contain.text", "0%")
    cy.get(':nth-child(1) > :nth-child(1) > :nth-child(1) > .ant-statistic > .ant-statistic-content')
        .should("contain.text", "20")
    cy.get(':nth-child(2) > :nth-child(1) > :nth-child(1) > .ant-statistic > .ant-statistic-content')
        .should("contain.text", "2")
  })
})