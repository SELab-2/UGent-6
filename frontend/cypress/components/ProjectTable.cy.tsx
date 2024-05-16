import ProjectTable from '../../src/pages/index/components/ProjectTable'

Cypress.on('uncaught:exception', (err: any, runnable: any) => {

  return false
})

const mockProjects = [
  {course: {name: "test course 1", url: "test course 1 url", courseId: 1908},
    deadline: "2024/05/28",
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
    deadline: "2024/06/03",
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

describe('ProjectTable', () => {
  it('renders loading correctly', () => {
    // see: https://on.cypress.io/mounting-react
    cy.mount(<ProjectTable projects={null}/>).should("exist")
    cy.get('.ant-spin-dot').should("be.visible")
  })
  it('renders projects', () => {
    // see: https://on.cypress.io/mounting-react
    cy.mount(<ProjectTable projects={mockProjects}/>).should("exist")
    cy.get('.ant-spin-dot').should("not.exist")
    cy.get("body").should("not.contain.text", "home.projects.noProjects")
  })
  it('renders no projects', () => {
    // see: https://on.cypress.io/mounting-react
    cy.mount(<ProjectTable projects={[]}/>).should("exist")
    cy.get('.ant-spin-dot').should("not.exist")
    cy.get("body").should("contain.text", "home.projects.noProjects")
        .and("contain.text", "home.projects.name")
        .and("contain.text", "home.projects.course")
        .and("contain.text", "home.projects.projectStatus")
        .and("contain.text", "home.projects.deadline")
        .and("contain.text", "home.projects.groupProgress")
  })

})