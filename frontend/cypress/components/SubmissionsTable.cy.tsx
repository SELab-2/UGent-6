import SubmissionsTable from '../../src/pages/project/components/SubmissionsTable'

describe('SubmissionsTable', () => {
  it('renders', () => {
    // see: https://on.cypress.io/mounting-react
    cy.mount(<SubmissionsTable submissions={[]} onChange={(s) => {}}/>).should("exist")
    cy.get("body").should("contain.text", "project.noSubmissions")
        .and("contain.text", "project.userName")
        .and("contain.text", "project.submission")
        .and("contain.text", "project.submissionTime")
        .and("contain.text", "project.status")
        .and("contain.text", "Score")
        .and("contain.text", "Download")
  })
})