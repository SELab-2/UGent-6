import Home from '../../src/pages/index/Home'
import {BrowserRouter} from "react-router-dom";

Cypress.on('uncaught:exception', (err: any, runnable: any) => {
  return false
})

describe('Home', () => {
  beforeEach(() => {
    cy.mount(<BrowserRouter><Home /></BrowserRouter>).should("exist")
    cy.viewport(1000, 600)
  })
  it('renders', () => {
    // see: https://on.cypress.io/mounting-react
    cy.get("body").should("contain.text", "home.yourCourses")
    cy.get(':nth-child(1) > .ant-card > .ant-card-body').should("be.visible")
    cy.get(':nth-child(2) > .ant-card > .ant-card-body').should("be.visible")
    cy.get(':nth-child(3) > .ant-card > .ant-card-body').should("be.visible")
  })

  it('shows projects by default', () => {
    // see: https://on.cypress.io/mounting-react
    cy.get("body").should("contain.text", "home.yourProjects")
    cy.get(".projectTable").should("be.visible")
    cy.get(".timeline").should("not.exist")
    cy.get(".calendar").should("not.exist")
  })
  it('can show a timeline', () => {
    // see: https://on.cypress.io/mounting-react
    cy.get("body").should("contain.text", "home.yourProjects")
    cy.get(':nth-child(2) > .ant-segmented-item-label').click()
    cy.get(".projectTable").should("not.exist")
    cy.get(".timeline").should("be.visible")
    cy.get(".calendar").should("not.exist")
  })
  it('can show a calendar', () => {
    // see: https://on.cypress.io/mounting-react
    cy.get("body").should("contain.text", "home.yourProjects")
    cy.get(':nth-child(3) > .ant-segmented-item-label').click()
    cy.get(".projectTable").should("not.exist")
    cy.get(".timeline").should("not.exist")
    cy.get(".calendar").should("be.visible")
  })
})