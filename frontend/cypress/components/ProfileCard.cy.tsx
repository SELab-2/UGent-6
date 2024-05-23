import ProfileCard from '../../src/pages/profile/components/ProfileCard'
import {User} from "../../src/providers/UserProvider";
import {BrowserRouter} from "react-router-dom";

const mockUser: User = {
  courseUrl:"courseURL",
  projects_url: "projectsURL",
  url : "URL",
  role: "student",
  email: "email",
  id: 1,
  name: "name",
  surname: "surname"
}

describe('ProfileCard', () => {
  it('renders', () => {
    // see: https://on.cypress.io/mounting-react
    cy.mount(<BrowserRouter><ProfileCard user={mockUser}/></BrowserRouter>)
    cy.get("body").should("contain.text", "name surname")
  })
})