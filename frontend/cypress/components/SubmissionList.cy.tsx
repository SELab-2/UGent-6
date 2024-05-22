import SubmissionList from '../../src/pages/project/components/SubmissionList'

describe('SubmissionList', () => {
  it('renders', () => {
    // see: https://on.cypress.io/mounting-react
    cy.mount(<SubmissionList submissions={[]}/>).should("exist")
    cy.get("body").should("contain.text", "project.noSubmissions")
        .and("contain.text", "project.submission")
        .and("contain.text", "project.submissionTime")
        .and("contain.text", "project.status")
  })
})