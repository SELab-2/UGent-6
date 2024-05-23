import React from 'react'
import GroupInfoModal from '../../src/pages/course/components/groupTab/GroupInfoModal'
import {ApiRoutes, } from "../../src/@types/requests.d";

Cypress.on('uncaught:exception', (err:any, runnable:any) => {
  return false
})

const mockGroup = {
  groupId: 1,
  capacity: 5,
  name: "Test group",
  groupClusterUrl: ApiRoutes.CLUSTER as ApiRoutes.CLUSTER,
  members:[{email: "test email", name: "TEST USER", userId: 1908}]
}

describe('GroupInfoModal', () => {
  it('renders', () => {
    // see: https://on.cypress.io/mounting-react
    cy.mount(<GroupInfoModal
        group={mockGroup}
        open={true}
        setOpen={(b: boolean) => {return}}
        removeUserFromGroup={(userId, groupId) => {return}}
    />).should("exist")
  })
})