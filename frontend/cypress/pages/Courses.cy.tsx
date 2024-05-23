import Courses from '../../src/pages/courses/Courses'
import {BrowserRouter} from "react-router-dom";

describe('Courses', () => {
  it('renders', () => {
    // see: https://on.cypress.io/mounting-react
    cy.mount(<BrowserRouter><Courses /></BrowserRouter>).should("exist")
    cy.get('.ant-card-head').should("contain.text", "courses.courses")
        .and("contain.text", "courses.sortAscending")
    cy.get('.ant-input').should("be.visible")
    cy.get('.ant-card-body').should("contain.text", "courses.noCourses")
  })
})