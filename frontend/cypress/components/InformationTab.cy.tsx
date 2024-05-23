import InformationTab from '../../src/pages/course/components/informationTab/InformationTab'

Cypress.on('uncaught:exception', (err, runnable) => {
  return false
})

describe('InformationTab', () => {
  it('renders', () => {
    // see: https://on.cypress.io/mounting-react
    cy.mount(<InformationTab />)
  })
})