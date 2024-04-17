

/**
 * Routes used in the app
 */
export enum AppRoutes {
  HOME = "/",
  COURSES = "/courses",
  PROJECT = "/courses/:courseId/projects/:projectId",
  PROJECT_CREATE = "/courses/:courseId/create",
  COURSE = "/courses/:courseId",
  NEW_SUBMISSION = "/courses/:courseId/projects/:projectId/submit",
  EDIT_PROJECT = "/courses/:courseId/projects/:projectId/edit",
  SUBMISSION = "/courses/:courseId/projects/:projectId/submissions/:submissionId",
  PROFILE = "/profile",
  ERROR = "/error",
  NOT_FOUND = "/not-found",
  EDIT_ROLE = "/edit-role",
  COURSE_INVITE = "/invite/:inviteId"
}
