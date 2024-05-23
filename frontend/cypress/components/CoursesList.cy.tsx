import CoursesList from '../../src/pages/courses/components/CoursesList'
import {CourseRelation} from "../../src/@types/requests";
import {BrowserRouter} from "react-router-dom";

const mockCourses = [
  {
    courseId: 1,
    name: "Test course 1",
    relation: "enrolled" as CourseRelation,
    memberCount: 1908,
    archivedAt: null,
    year: 2023,
    url: "Test course 1 url"
  },{
    courseId: 2,
    name: "Test course 2",
    relation: "enrolled" as CourseRelation,
    memberCount: 35,
    archivedAt: null,
    year: 2023,
    url: "Test course 2 url"
  }
]

describe('CoursesList', () => {
  it('renders with no courses', () => {
    // see: https://on.cypress.io/mounting-react
    cy.mount(<BrowserRouter><CoursesList courses={[]}/></BrowserRouter>).should("exist")
    cy.get("body").should("contain.text", "courses.noCourses")
  })

  it('renders with courses', () => {
    // see: https://on.cypress.io/mounting-react
    cy.mount(<BrowserRouter><CoursesList courses={mockCourses} role="enrolled"/></BrowserRouter>).should("exist")
    cy.get("body").should("not.contain.text", "courses.noCourses")
        .and("contain.text", "Test course 1")
        .and("contain.text", "Test course 2")
        .and("contain.text", "2023 - 2024")
        .and("contain.text", "1908 courses.members")
        .and("contain.text", "35 courses.members")
  })
})