import EditRole from '../../src/pages/editRole/EditRole'

describe('EditRole', () => {
  it('renders', () => {
    // see: https://on.cypress.io/mounting-react
    cy.mount(<EditRole />).should("exist")
    cy.get("body").should("contain.text", "editRole.name")
        .and("contain.text", "editRole.searchTutorial")
  })
})