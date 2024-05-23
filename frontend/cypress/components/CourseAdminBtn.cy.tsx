import CourseAdminBtn from '../../src/pages/course/components/tabExtraBtn/CourseAdminBtn'
import {BrowserRouter} from "react-router-dom";

Cypress.on('uncaught:exception', (err, runnable) => {
  return false
})

describe('<CourseAdminBtn />', () => {
  it('renders', () => {
    // see: https://on.cypress.io/mounting-react
    cy.mount(<BrowserRouter><CourseAdminBtn courseId="1"/></BrowserRouter>).should("exist")
  })
})