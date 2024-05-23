import GradesCard from '../../src/pages/course/components/gradesTab/GradesCard'

Cypress.on('uncaught:exception', (err:any, runnable:any) => {

  return false
})
describe('GradesCard', () => {
  it('renders', () => {
    // see: https://on.cypress.io/mounting-react
    cy.mount(<GradesCard />).should("exist")
  })
})