import ProjectCard from '../../src/pages/index/components/ProjectCard'
import {BrowserRouter} from "react-router-dom";
Cypress.on('uncaught:exception', (err: any, runnable: any) => {

  return false
})
describe('<ProjectCard />', () => {
  it('renders', () => {
    // see: https://on.cypress.io/mounting-react
    cy.mount(<BrowserRouter><ProjectCard courseId={undefined}/></BrowserRouter>).should("exist")
  })
})