import Profile from '../../src/pages/profile/Profile'
import {User} from "../../src/providers/UserProvider";

Cypress.on('uncaught:exception', (err: any, runnable: any) => {
  return false
})

describe('Profile', () => {
  it('renders', () => {
    // see: https://on.cypress.io/mounting-react
    cy.mount(<Profile />).should("exist")
  })
})