import ProjectStatusTag from '../../src/pages/index/components/ProjectStatusTag'

describe('ProjectStatusTag', () => {
  it('renders when correct', () => {
    // see: https://on.cypress.io/mounting-react
    cy.mount(<ProjectStatusTag status="correct" icon={true}/>).should("exist")
    cy.get("body").should("contain.text", "home.projects.status.completed")
  })
  it('renders when incorrect', () => {
    // see: https://on.cypress.io/mounting-react
    cy.mount(<ProjectStatusTag status="incorrect" icon={true}/>).should("exist")
    cy.get("body").should("contain.text", "home.projects.status.failed")
        .and("not.contain.text", "home.projects.status.completed")
  })
  it('renders when not started', () => {
    // see: https://on.cypress.io/mounting-react
    cy.mount(<ProjectStatusTag status="not started" icon={true}/>).should("exist")
    cy.get("body").should("contain.text", "home.projects.status.notStarted")
        .and("not.contain.text", "home.projects.status.completed")
  })
})