import Course from '../../src/pages/course/Course'
import {BrowserRouter} from "react-router-dom";

Cypress.on('uncaught:exception', (err, runnable) => {
  return false
})

describe('<Course />', () => {
  it('renders', () => {
    // see: https://on.cypress.io/mounting-react
    cy.mount(<BrowserRouter><Course /></BrowserRouter>).should("exist")
  })
})