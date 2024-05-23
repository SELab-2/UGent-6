import Submit from '../../src/pages/submit/Submit'
import {BrowserRouter} from "react-router-dom";

describe('Submit', () => {
  it('renders', () => {
    // see: https://on.cypress.io/mounting-react
    cy.mount(<BrowserRouter><Submit /></BrowserRouter>).should("exist")
    cy.get("body").should("contain.text", "project.files")
  })
  it('disables submit button by default', () => {
    // see: https://on.cypress.io/mounting-react
    cy.mount(<BrowserRouter><Submit /></BrowserRouter>).should("exist")
    cy.get('.ant-btn-default').should("be.visible").and("not.be.disabled")
    cy.get('.ant-btn-primary').should("be.visible").and("be.disabled")
  })
})