import LanguageDropdown from '../../src/components/LanguageDropdown'

describe('<LanguageDropdown />', () => {
  it('renders', () => {
    // see: https://on.cypress.io/mounting-react
    cy.mount(<LanguageDropdown />)
    cy.get('.Dropdown').should("exist")
  })

  it('functions', () => {
    // see: https://on.cypress.io/mounting-react
    cy.mount(<LanguageDropdown />)
    cy.get("body").should("not.contain.text", "English")
        .and("not.contain.text", "Nederlands")
    cy.get('.Dropdown').trigger('mouseover')
    cy.get("body").should("contain.text", "English")
        .and("contain.text", "Nederlands")
  })
})