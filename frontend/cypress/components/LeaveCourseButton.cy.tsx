import LeaveCourseButton from '../../src/pages/course/components/tabExtraBtn/LeaveCourseButton'
import {BrowserRouter} from "react-router-dom";

describe('<LeaveCourseButton />', () => {
  it('renders', () => {
    // see: https://on.cypress.io/mounting-react
    cy.mount(<BrowserRouter><LeaveCourseButton courseId={"1"}/></BrowserRouter>).should("exist")
    cy.get('.ant-btn').should("contain.text", "course.leave")
    cy.get('.ant-btn').click()
    cy.get("body").should("contain.text", "course.leave")
        .and("contain.text", "course.leaveConfirm")
        .and("contain.text", "OK")
        .and("contain.text", "Cancel")
  })
})