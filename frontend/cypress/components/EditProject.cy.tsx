import EditProject from '../../src/pages/editProject/EditProject'
import {BrowserRouter} from "react-router-dom";

describe('<EditProject />', () => {
  it('renders', () => {
    // see: https://on.cypress.io/mounting-react
    cy.mount(<BrowserRouter><EditProject /></BrowserRouter>).should("exist")
  })
})