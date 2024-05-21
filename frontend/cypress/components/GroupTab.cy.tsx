import GroupTab from '../../src/pages/project/components/GroupTab'

describe('GroupTab', () => {
  it('renders', () => {
    // see: https://on.cypress.io/mounting-react
    cy.mount(<GroupTab />).should("exist")
  })
})