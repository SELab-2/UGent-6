import React from 'react'
import GroupsCard from '../../src/pages/course/components/groupTab/GroupsCard'


describe('<GroupsCard />', () => {
  it('loads', () => {
    // see: https://on.cypress.io/mounting-react
    cy.mount(<GroupsCard courseId={1908}/>)
    cy.get("body").should("contain.text", "Loading")
  })
})