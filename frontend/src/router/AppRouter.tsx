import { Routes, Route } from "react-router-dom"
import Home from "../pages/index/Home"
import Dashboard from "../pages/dashboard/Dashboard"
import Profile from "../pages/profile/Profile"
import { AppRoutes } from "../@types/routes"
import ApiTest from "../pages/apiTest/ApiTest"
import AuthenticatedRoute from "./AuthenticatedRoute"
import Course from "../pages/course/Course"
import CourseRoutes from "./CourseRoutes"

const AppRouter = () => {
  return (
    <Routes>
      <Route
        path={AppRoutes.HOME}
        element={<Home />}
      />

      <Route path="/" element={<AuthenticatedRoute />}>
        <Route
          path={AppRoutes.DASHBOARD}
          element={<Dashboard />}
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
