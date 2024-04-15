

/**
 * Routes used in the app
 */
export enum AppRoutes {
  HOME = "/",
  COURSES = "/courses",
  PROJECT = "/courses/:courseId/projects/:projectId",
  COURSE = "/courses/:courseId",
  SUBMISSION_FEEDBACK = "/courses/:courseId/projects/:projectId/feedback/:submitionId",
  NEW_SUBMISSION = "/courses/:courseId/projects/:projectId/submit",
  EDIT_PROJECT = "/courses/:courseId/projects/:projectId/edit",
  SUBMISSION = "/submissions/:submissionId",
  PROFILE = "/profile",
  EDIT_ROLE = "/edit-role",
}

