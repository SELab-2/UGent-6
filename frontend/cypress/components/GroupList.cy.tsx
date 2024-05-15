import GroupList from '../../src/pages/course/components/groupTab/GroupList'
import {ApiRoutes} from "../../src/@types/requests.d";

const mockGroups = [
  {
    groupId: 1,
    capacity: 5,
    name: "Test group 1",
    groupClusterUrl: ApiRoutes.CLUSTER as ApiRoutes.CLUSTER,
    members:[{email: "test email 1", name: "TEST USER 1", userId: 1908}]
  },
  {
    groupId: 2,
    capacity: 5,
    name: "Test group 2",
    groupClusterUrl: ApiRoutes.CLUSTER as ApiRoutes.CLUSTER,
    members:[{email: "test email 2", name: "TEST USER 2", userId: 35}]
  }]

describe('GroupList', () => {
  it('renders', () => {
    // see: https://on.cypress.io/mounting-react
    cy.mount(<GroupList groups={mockGroups}/>).should("exist")
    cy.get("body").should("contain.text", "Test group 1")
        .and("contain.text", "Test group 2")
  })
})