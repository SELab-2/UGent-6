import SettingsCard from '../../src/pages/course/components/settingsTab/SettingsCard'
import {BrowserRouter} from "react-router-dom";

Cypress.on('uncaught:exception', (err, runnable) => {
  return false
})

describe('<SettingsCard />', () => {
  it('renders', () => {
    // see: https://on.cypress.io/mounting-react
    cy.mount(<BrowserRouter> <SettingsCard /></BrowserRouter>).should("exist")
  })
})