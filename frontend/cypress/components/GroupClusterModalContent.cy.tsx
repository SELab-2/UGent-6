import GroupClusterModalContent from '../../src/pages/projectCreate/components/GroupClusterModalContent'

describe('GroupClusterModalContent', () => {
  it('renders', () => {
    // see: https://on.cypress.io/mounting-react
    cy.mount(<GroupClusterModalContent courseId={1} onClusterCreated={(c) => {}} onClose={()=>{}}/>).should("exist")
    cy.get("#name").type("TEST")
    cy.get("body").should("contain.text", "project.change.clusterName")
        .and("contain.text", "project.change.amountOfGroups")
        .and("contain.text", "project.change.groupSize")
    cy.get(':nth-child(1) > .ant-btn').should("be.visible")
    cy.get(':nth-child(2) > .ant-btn').should("be.visible")
  })
})