import { Routes, Route } from "react-router-dom"
import Profile from "../pages/profile/Profile"
import { AppRoutes } from "../@types/routes"
import ApiTest from "../pages/apiTest/ApiTest"
import Course from "../pages/course/Course"
import CourseRoutes from "./CourseRoutes"
import HomeAuthCheck from "../pages/index/HomeAuthCheck"
import Project from "../pages/project/Project"
import EditRole from "../pages/editRole/EditRole"
import Submit from "../pages/submit/Submit"
import ProjectRoutes from "./ProjectRoutes"
import Error from "../pages/error/Error"
import ProjectCreate from "../pages/projectCreate/ProjectCreate"
import AdminView from "../hooks/AdminView"
import CourseInvite from "../pages/courseInvite/CourseInvite"
import Submission from "../pages/submission/Submission"
import AuthenticatedRoute from "./AuthenticatedRoute"
import CourseAdminView from "../hooks/CourseAdminView"
// import ProjectTestsPage from "../pages/projectTest_old/ProjectTestPage";
import Courses from "../pages/courses/Courses"
import EditProject from "../pages/editProject/EditProject"
import ExtraFilesDownload from "../pages/editProject/extrafilesDownload/ExtaFilesDownload"

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
        {/* ========================= Authenticated Routes ========================= */}
        <Route
          path={AppRoutes.COURSE_INVITE}
          element={<CourseInvite />}
        />


        <Route
          path={AppRoutes.PROFILE}
          element={<Profile />}
        />
        <Route
          path={AppRoutes.EDIT_ROLE}
          element={
            <AdminView>
              <EditRole />
            </AdminView>
          }
        />

          <Route>
            <Route
              path={AppRoutes.COURSES}
              element={<Courses/>}
            />
          </Route>

        <Route
          path={AppRoutes.COURSE}
          element={<CourseRoutes />}
        >
          {/* ========================= Course Routes ========================= */}
          <Route
            path=""
            element={<Course />}
          />

          
            <Route
              path={AppRoutes.PROJECT_CREATE}
              element={<CourseAdminView><ProjectCreate /> </CourseAdminView>}
            />
         

          <Route
            path={AppRoutes.PROJECT}
            element={<ProjectRoutes />}
          >
            {/* ========================= Project Routes ========================= */}
            <Route
              path={AppRoutes.PROJECT}
              element={<Project />}
            />
            <Route 
              path={AppRoutes.DOWNLOAD_PROJECT_TESTS}
              element={<ExtraFilesDownload/>}
            />
          

            {/* <Route
            path={AppRoutes.SUBMISSION_FEEDBACK}
            element={<Feedback />}
            /> */}

            <Route
              path={AppRoutes.NEW_SUBMISSION}
              element={<Submit />}
            />

            <Route
              path={AppRoutes.SUBMISSION}
              element={<Submission />}
            />
            <Route 
              path={AppRoutes.EDIT_PROJECT}
              element={<EditProject/>}
            />
            {/* <Route
              path={AppRoutes.PROJECT_TESTS}
              element={<ProjectTestsPage/>}
            /> */}
          </Route>
        </Route>

        <Route
          path="/api-test"
          element={<ApiTest />}
        />

        {/*De 404 errorpagina route, deze moet de laatste lijn staan, routes hieronder worden nooit gematchd!*/}
        <Route
          path={"*"}
          element={<Error errorCode={404} />}
        />
      </Route>
    </Routes>
  )
}

export default AppRouter
