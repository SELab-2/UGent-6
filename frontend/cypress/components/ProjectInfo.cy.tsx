import ProjectInfo from '../../src/pages/index/components/ProjectInfo'
const mockProject = {course: {name: "test course 1", url: "test course 1 url", courseId: 1908},
  deadline: "2023/05/15",
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
}

describe('<ProjectInfo />', () => {
  it('renders', () => {
    // see: https://on.cypress.io/mounting-react
    cy.mount(<ProjectInfo project={mockProject}/>).should("exist")
    cy.get("body").should("contain.text", "home.projects.name")
        .and("contain.text", "test course 1")
        .and("contain.text", "home.projects.deadline")
        .and("contain.text", "15 mei 2023")
        .and("contain.text", "home.projects.description")
        .and("contain.text", "do something")
        .and("contain.text", "home.projects.projectStatus")
        .and("contain.text", "home.projects.groupProgress")
        .and("contain.text", "50%")
  })
})