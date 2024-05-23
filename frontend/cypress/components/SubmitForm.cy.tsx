import SubmitForm from '../../src/pages/submit/components/SubmitForm'

describe('SubmitForm', () => {
  it('renders', () => {
    // see: https://on.cypress.io/mounting-react
    cy.mount(<SubmitForm/>).should("exist")
    cy.get("body").should("contain.text", "project.uploadAreaTitle")
        .and("contain.text", "project.uploadAreaSubtitle")
  })
  it('opens a file window when clicked', () => {
        // see: https://on.cypress.io/mounting-react
        cy.mount(<SubmitForm />).should("exist")
        cy.get('.ant-upload-drag-container').click()
  });
})