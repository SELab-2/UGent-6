import Navbar from '../../src/pages/index/landing/Navbar'
import {BrowserRouter} from "react-router-dom";

describe('Navbar', () => {
  it('renders', () => {
    // see: https://on.cypress.io/mounting-react
    cy.mount(<BrowserRouter><Navbar onLogin={() => {}}/></BrowserRouter>).should("exist")
    cy.get(".navbar").should("be.visible")
        .and("contain.text", "Pigeonhole")
    cy.get(".landing-page-btn").should("be.visible")
    cy.get(".white-color").should("be.visible")
  })
})