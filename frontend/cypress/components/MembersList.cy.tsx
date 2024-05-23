import MembersList from '../../src/pages/course/components/membersTab/MembersList'

Cypress.on('uncaught:exception', (err, runnable) => {
  return false
})

describe('<MembersList />', () => {
  it('renders', () => {
    // see: https://on.cypress.io/mounting-react
    cy.mount(<MembersList />).should("exist")
  })
})