

/**
 * Routes used in the app
 */
export enum AppRoutes {
  HOME = "/",
  COURSES = "/courses",
  PROJECT = "/courses/:courseId/projects/:projectId",
  JOIN_COURSE = "/courses/:joinKey/join",
  COURSE = "/courses/:courseId",
  NEW_SUBMISSION = "/courses/:courseId/projects/:projectId/submit",
  SUBMISSION_FEEDBACK = "/courses/:courseId/projects/:projectId/feedback/:submitionId",
  PROFILE = "/profile",
  }

