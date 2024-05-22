import MembersCard from '../../src/pages/course/components/membersTab/MemberCard'

Cypress.on('uncaught:exception', (err, runnable) => {
  return false
})
describe('<MembersCard />', () => {
  it('renders', () => {
    // see: https://on.cypress.io/mounting-react
    cy.mount(<MembersCard />).should("exist")
  })
})