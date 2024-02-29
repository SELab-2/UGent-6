


/**
 * Routes used to make API calls
 */
export enum ApiRoutes {
  COURSES = "api/courses",  // example
   TEST = "api/test"
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

