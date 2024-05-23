import CourseSection from '../../src/pages/index/components/CourseSection'
import {BrowserRouter} from "react-router-dom";

Cypress.on('uncaught:exception', (err: any, runnable: any) => {
  return false
})

describe('<CourseSection />', () => {
  it('renders', () => {
    // see: https://on.cypress.io/mounting-react
    cy.mount(<BrowserRouter><CourseSection /></BrowserRouter>).should("exist")
  })
})