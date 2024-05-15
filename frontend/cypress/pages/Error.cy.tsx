import Error from '../../src/pages/error/Error'
import {BrowserRouter} from "react-router-dom";


describe('ErrorPage', () => {
  it('renders', () => {
    cy.mount(<BrowserRouter><Error errorCode={404} errorMessage={"TEST"}/></BrowserRouter>)
    cy.get("Button").should("exist")
    cy.get("body").should("contain.text", "404")
    cy.get("body").should("contain.text", "TEST")
  });
});