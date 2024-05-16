import EditRole from '../../src/pages/editRole/EditRole'

describe('EditRole', () => {
  it('renders', () => {
    // see: https://on.cypress.io/mounting-react
    cy.mount(<EditRole />).should("exist")
    cy.get("body").should("contain.text", "Alice Kornelis")
        .and("contain.text", "Bob Kornelis")
        .and("contain.text", "Charlie Kornelis")
    cy.get('.ant-list-items > :nth-child(1)').should("contain.text", "editRole.student")
    cy.get('.ant-list-items > :nth-child(2)').should("contain.text", "editRole.teacher")
    cy.get('.ant-list-items > :nth-child(3)').should("contain.text", "editRole.admin")
  })
})