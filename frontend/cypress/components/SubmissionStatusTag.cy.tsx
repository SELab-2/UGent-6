import SubmissionStatusTag, {SubmissionStatus} from '../../src/pages/project/components/SubmissionStatusTag'

describe('SubmissionStatusTag', () => {
  it('renders when passed', () => {
    // see: https://on.cypress.io/mounting-react
    cy.mount(<SubmissionStatusTag status={SubmissionStatus.PASSED}/>)
    cy.get("body").should("contain.text", "project.passed")
        .and("not.contain.text", "project.testFailed")
        .and("not.contain.text", "project.notSubmitted")
  })
  it('renders when failed', () => {
    // see: https://on.cypress.io/mounting-react
    cy.mount(<SubmissionStatusTag status={SubmissionStatus.DOCKER_REJECTED}/>)
    cy.get("body").should("contain.text", "project.testFailed")
        .and("not.contain.text", "project.passed")
        .and("not.contain.text", "project.notSubmitted")
  })
  it('renders when not submitted', () => {
    // see: https://on.cypress.io/mounting-react
    cy.mount(<SubmissionStatusTag status={SubmissionStatus.NOT_SUBMITTED}/>)
    cy.get("body").should("not.contain.text", "project.passed")
        .and("not.contain.text", "project.testFailed")
        .and("contain.text", "project.notSubmitted")
  })
})