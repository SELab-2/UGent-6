

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
  SUBMISSION_FEEDBACK = "/courses/:courseId/projects/:projectId/feedback/:submitionId",
  PROFILE = "/profile",
  ERROR = "/error",
  NOT_FOUND = "/not-found"
  }

