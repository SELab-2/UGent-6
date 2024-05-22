import GradesList from '../../src/pages/course/components/gradesTab/GradesList'
import {CourseGradesType} from "../../src/pages/course/components/gradesTab/GradesCard";
import {BrowserRouter} from "react-router-dom";

const mockGrades:CourseGradesType[] = [{
  projectName: "Test Project",
  projectUrl: "Project URL",
  projectId: 1908,
  maxScore: 100,
  groupFeedback: {score: 95, feedback: "Goed gedaan", groupId:1, projectId:1908}
}]

describe('GradesList', () => {
  it('renders', () => {
    // see: https://on.cypress.io/mounting-react
    cy.mount(<BrowserRouter><GradesList feedback={mockGrades} courseId={1}/></BrowserRouter>)
    cy.get("body").should("contain.text", "Feedback")
    cy.get(".ant-list-item").should("exist")
        .and("contain.text", "Test Project")
        .and("contain.text", "Goed gedaan")
        .and("contain.text", "95 / 100")
  })
})