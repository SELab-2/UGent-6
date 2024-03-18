import { Routes, Route } from "react-router-dom"
import Profile from "../pages/profile/Profile"
import { AppRoutes } from "../@types/routes"
import ApiTest from "../pages/apiTest/ApiTest"
import AuthenticatedRoute from "./AuthenticatedRoute"
import Course from "../pages/course/Course"
import CourseRoutes from "./CourseRoutes"
import HomeAuthCheck from "../pages/index/HomeAuthCheck"
import Project from "../pages/project/Project"

const AppRouter = () => {
  return (
    <Routes>
      <Route
        path={AppRoutes.HOME}
        element={<HomeAuthCheck />}
      />

      <Route path="/" element={<AuthenticatedRoute />}>
   
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
            element={<Project />}
            />
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
