import GroupProgress from '../../src/pages/index/components/GroupProgress'

describe('GroupProgress', () => {
  it('renders', () => {
    // see: https://on.cypress.io/mounting-react
    cy.mount(<GroupProgress usersCompleted={10} userCount={20}/>).should("exist")
    cy.get("body").should("be.visible")
        .and("contain.text", "50%")
  })
})