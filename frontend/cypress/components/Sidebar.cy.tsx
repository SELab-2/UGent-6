import Sidebar from '../../src/components/layout/sidebar/Sidebar'
import {BrowserRouter} from "react-router-dom";

describe('<Sidebar />', () => {
  it('renders', () => {
    // see: https://on.cypress.io/mounting-react
    cy.mount(<BrowserRouter><Sidebar /> </BrowserRouter>)
    cy.get("Button").should("exist")
  })

  it("functions", () => {
    cy.mount(<BrowserRouter><Sidebar /> </BrowserRouter>)
    cy.get("body").should("not.contain.text", "Profile")
        .and("not.contain.text", "home.allCourses")
    cy.get("Button").click()
    cy.get("body").should("contain.text", "Profile")
        .and("contain.text", "home.allCourses")
  })
})