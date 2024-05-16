import CreateCourseModal from '../../src/pages/index/components/CreateCourseModal'
import {BrowserRouter} from "react-router-dom";

describe('CreateCourseModal', () => {
  it('renders', () => {
    // see: https://on.cypress.io/mounting-react
    cy.mount(<BrowserRouter><CreateCourseModal open={true} setOpen={(b: boolean) => {}}/></BrowserRouter>).should("exist")
    cy.get("body").should("contain.text", "home.createCourse")
        .and("contain.text", "home.courseName")
        .and("contain.text", "project.change.description")
        .and("contain.text", "components.write")
        .and("contain.text", "components.preview")
    cy.get('.ant-btn-default').should("be.visible")
    cy.get('.ant-btn-primary').should("be.visible")
  })
})