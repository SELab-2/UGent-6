


/**
 * Routes used to make API calls
 */
export enum ApiRoutes {
   COURSES = "api/courses"  // example

}


/**
 * Routes used in the app
 */
export enum AppRoutes {
  HOME = "/",
  COURSES = "/courses",
  DASHBOARD = "/dashboard",
  COURSE = "/courses/:id",
  LOGIN = "/login",
  LOGOUT = "/logout",
  PROFILE = "/profile",
  ERROR = "/error",
  NOT_FOUND = "/not-found"
}



/**
 *  the body of the POST requests
 */
export type POST_Requests = {
  [ApiRoutes.COURSES]: {
      name: string
  }
}

/**
 * the body of the PUT requests
 */
export type PUT_Requests = {
  [ApiRoutes.COURSES]: {
      name: string
  }
}