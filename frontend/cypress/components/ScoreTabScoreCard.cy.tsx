import ScoreCard from '../../src/pages/project/components/ScoreTab'

describe('ScoreCard', () => {
  it('renders', () => {
    // see: https://on.cypress.io/mounting-react
    cy.mount(<ScoreCard />).should("exist")
  })
})