import UserList from '../../src/pages/editRole/components/UserList'
import {UsersType} from "../../src/pages/editRole/EditRole";

const mockUsers : UsersType[] = [
  {
    name: "Test user 1", userId: 1908, url: "test/user1", email: "user1@test.com", role: "student"
  },
  {
    name: "Test user 2", userId: 35, url: "test/user2", email: "user2@test.com", role: "teacher"
  }
]

describe('UserList', () => {
  it('renders without users', () => {
    // see: https://on.cypress.io/mounting-react
    cy.mount(<UserList users={[]} updateRole={(user, role) => {return}}/>)
        .should("exist")
    cy.get("body").should("contain.text", "No data")
  })

  it('renders with users', () => {
    cy.mount(<UserList users={mockUsers} updateRole={(user, role) => {return}}/>)
        .should("exist")
    cy.get("body").should("not.contain.text", "No data")
        .and("contain.text", "Test user 1")
        .and("contain.text", "Test user 2")
    cy.get('.ant-list-items > :nth-child(1)').should("contain.text", "editRole.student")
    cy.get('.ant-list-items > :nth-child(2)').should("contain.text", "editRole.teacher")
  })
})