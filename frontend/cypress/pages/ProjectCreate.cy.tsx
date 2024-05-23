import ProjectCreate from '../../src/pages/projectCreate/ProjectCreate'
import {BrowserRouter} from "react-router-dom";

describe('ProjectCreate', () => {
  it('renders', () => {
    // see: https://on.cypress.io/mounting-react
    cy.mount(<BrowserRouter><ProjectCreate /></BrowserRouter>).should("exist")
    cy.get("body").should("contain.text", "project.change.title")
  })

  it('shows message for empty required fields', () => {
    // see: https://on.cypress.io/mounting-react
    cy.mount(<BrowserRouter><ProjectCreate /></BrowserRouter>).should("exist")
    cy.get("body").should("not.contain.text", "project.change.nameMessage")
    cy.get("#name").click()
    cy.get("body").click()
    cy.get("body").should("contain.text", "project.change.nameMessage")
    cy.get("#name").type("Test name")
    cy.get("body").click()
    cy.get("body").should("not.contain.text", "project.change.nameMessage")
  })

  it('shows the description in the preview', () => {
    // see: https://on.cypress.io/mounting-react
    cy.mount(<BrowserRouter><ProjectCreate /></BrowserRouter>).should("exist")
    cy.get("#description").type("Test description")
    cy.get('[data-node-key="preview"]').click()
    cy.get("body").should("contain.text", "Test description")
  })

  it('can switch between tabs', () => {
    // see: https://on.cypress.io/mounting-react
    cy.mount(<BrowserRouter><ProjectCreate /></BrowserRouter>).should("exist")
    cy.get("#name").should("be.visible")
    cy.get(".ant-select-selector").should("not.be.visible")
    cy.get("#structureTest").should("not.be.visible")
    cy.get("#rc-tabs-6-tab-groups").click()
    cy.get("#name").should("not.be.visible")
    cy.get(".ant-select-selector").should("be.visible")
    cy.get("#structureTest").should("not.be.visible")
    // idk why maar als ik 1maal klik dan switcht hij niet
    cy.get('#rc-tabs-6-tab-structure').click()
    cy.get('#rc-tabs-6-tab-structure').click()
    cy.get("#name").should("not.be.visible")
    cy.get(".ant-select-selector").should("not.be.visible")
    cy.get("#structureTest").should("be.visible")
  })
})