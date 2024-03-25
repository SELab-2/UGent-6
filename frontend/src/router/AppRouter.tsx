import { Routes, Route } from "react-router-dom"
import Profile from "../pages/profile/Profile"
import { AppRoutes } from "../@types/routes"
import ApiTest from "../pages/apiTest/ApiTest"
import AuthenticatedRoute from "./AuthenticatedRoute"
import Course from "../pages/course/Course"
import CourseRoutes from "./CourseRoutes"
import HomeAuthCheck from "../pages/index/HomeAuthCheck"
import Project from "../pages/project/Project"
import Submit from "../pages/submit/Submit"
import Feedback from "../pages/feedback/Feedback"
import ProjectRoutes from "./ProjectRoutes"

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
          path={AppRoutes.NEW_SUBMISSION}
          element={<Submit />}
        />

        <Route
          path={AppRoutes.PROFILE}
          element={<Profile />}
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
          </Route>
        </Route>
        <Route
          path="/api-test"
          element={<ApiTest />}
        />
      </Route>
    </Routes>
  )
}

export default AppRouter
