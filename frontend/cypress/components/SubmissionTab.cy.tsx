import SubmissionTab from '../../src/pages/project/components/SubmissionTab'

describe('SubmissionTab', () => {
  it('renders when loading', () => {
    // see: https://on.cypress.io/mounting-react
    cy.mount(<SubmissionTab projectId={1} courseId={1}/>).should("exist")
    cy.get(".ant-spin-dot").should("be.visible")
  })
})