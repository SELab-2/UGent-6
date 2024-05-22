import Submission from '../../src/pages/submission/Submission'

describe('Submission', () => {
  it('renders', () => {
    // see: https://on.cypress.io/mounting-react
    cy.mount(<Submission />).should("exist")
    cy.get(".ant-spin-dot").should("be.visible")
  })
})