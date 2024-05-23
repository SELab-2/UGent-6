import ExtraTabBtn from '../../src/pages/course/components/tabExtraBtn/ExtraTabBtn'

Cypress.on('uncaught:exception', (err, runnable) => {
  // returning false here prevents Cypress from
  // failing the test
  return false
})

describe('<ExtraTabBtn />', () => {
  it('renders', () => {
    // see: https://on.cypress.io/mounting-react
    cy.mount(<ExtraTabBtn />).should("exist")
  })
})