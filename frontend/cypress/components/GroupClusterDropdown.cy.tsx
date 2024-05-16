import GroupClusterDropdown from '../../src/pages/projectCreate/components/GroupClusterDropdown'

describe('GroupClusterDropdown', () => {
  it('renders', () => {
    // see: https://on.cypress.io/mounting-react
    cy.mount(<GroupClusterDropdown courseId={1}/>).should("exist")
    cy.get(".ant-select-selector").should("be.visible")
    cy.get(".ant-select-selector").click()
    cy.get("body").should("contain.text", "no")
  })
})