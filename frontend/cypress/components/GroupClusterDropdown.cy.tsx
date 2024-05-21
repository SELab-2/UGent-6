import GroupClusterDropdown from '../../src/pages/projectCreate/components/GroupClusterDropdown'

Cypress.on('uncaught:exception', (err:any, runnable:any) => {
  return false
})

describe('GroupClusterDropdown', () => {
  it('renders', () => {
    // see: https://on.cypress.io/mounting-react
    cy.mount(<GroupClusterDropdown courseId={1}/>).should("exist")
    cy.get(".ant-select-selector").should("be.visible")
  })
})