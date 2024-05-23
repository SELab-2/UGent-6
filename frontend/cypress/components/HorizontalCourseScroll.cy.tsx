import HorizontalCourseScroll from '../../src/pages/index/components/HorizontalCourseScroll'
import {BrowserRouter} from "react-router-dom";

describe('HorizontalCourseScroll', () => {
  it('renders', () => {
    // see: https://on.cypress.io/mounting-react
    cy.mount(<BrowserRouter>
      <HorizontalCourseScroll
        title="test horizontal scroll"
        projects={null}
        type="test"
        onOpenNew={()=> {}}
        showMore={true}
        showPlus={true}
        allOptions={true}
      />
    </BrowserRouter>).should("exist")
    cy.get("body").should("contain.text", "test horizontal scroll")
        .and("contain.text", "home.moreCourses")
    cy.get(':nth-child(1) > .ant-card > .ant-card-body').should("be.visible")
    cy.get(':nth-child(2) > .ant-card > .ant-card-body').should("be.visible")
    cy.get(':nth-child(3) > .ant-card > .ant-card-body').should("not.be.visible")
    cy.viewport(2560, 1440)
    cy.get(':nth-child(3) > .ant-card > .ant-card-body').should("be.visible")
  })
})