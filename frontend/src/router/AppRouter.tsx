import { Routes, Route } from "react-router-dom"
import Profile from "../pages/profile/Profile"
import { AppRoutes } from "../@types/routes"
import ApiTest from "../pages/apiTest/ApiTest"
import AuthenticatedRoute from "./AuthenticatedRoute"
import Course from "../pages/course/Course"
import CourseRoutes from "./CourseRoutes"
import HomeAuthCheck from "../pages/index/HomeAuthCheck"
import Project from "../pages/project/Project"
import EditRole from "../pages/editRole/EditRole"
import Submit from "../pages/submit/Submit"
import Submission from "../pages/submission/Submission"
import Feedback from "../pages/feedback/Feedback"
import ProjectRoutes from "./ProjectRoutes"
import Error from "../pages/error/Error";



const AppRouter = () => {
  return (
    <Routes>
      <Route
        path={AppRoutes.HOME}
        element={<HomeAuthCheck />}
      />

      <Route
        path="/"
        element={<AuthenticatedRoute />}
      >


      <Route
        path={AppRoutes.SUBMISSION}
        element={<Submission />}
      />

       <Route
         path={AppRoutes.NEW_SUBMISSION}
         element={<Submit />}
       />

      <Route
        path={AppRoutes.PROFILE}
        element={<Profile />}
      />
      <Route
        path={AppRoutes.EDIT_ROLE}
        element={<EditRole />}
      />
      <Route
        path={AppRoutes.COURSE}
        element={<CourseRoutes />}
      >

        <Route
          path=""
          element={<Course />}
        />

        <Route
          path={AppRoutes.PROJECT}
          element={<ProjectRoutes />}
        >
          <Route
            path={AppRoutes.PROJECT}
            element={<Project />}
          />

          <Route
            path={AppRoutes.SUBMISSION_FEEDBACK}
            element={<Feedback />}
            />

            <Route
              path={AppRoutes.NEW_SUBMISSION}
              element={<Submit />}
            />
          </Route>
        </Route>

        <Route
          path="/api-test"
          element={<ApiTest />}
        />

        {/*De 404 errorpagina route, deze moet de laatste lijn staan, routes hieronder worden nooit gematchd!*/}
        <Route 
          path={"*"}
          element={<Error errorCode={404}/>}
        />

      </Route>
    </Routes>
  )
}

export default AppRouter
