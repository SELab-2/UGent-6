import Project from '../../src/pages/project/Project'

Cypress.on('uncaught:exception', (err: any, runnable: any) => {
  return false
})

describe('Project', () => {
  it('renders', () => {
    // see: https://on.cypress.io/mounting-react
    cy.mount(<Project />).should("exist")
  })
})