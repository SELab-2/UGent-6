


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
 * The response you get from the POST request
 */
export type POST_Responses = {
  [ApiRoutes.COURSES]: {
      id: string
  }
}

/**
 *  the body of the DELETE requests
 */
export type DELETE_Requests = {
  [ApiRoutes.COURSES]: {
      name: string //TODO: 
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

/**
 * The response you get from the GET request
 */
export type GET_Responses = {
  [ApiRoutes.COURSES]: {
      id: string
      name: string
  },
  [ApiRoutes.TEST]: {
    name: string
    firstName: string
    lastName: string
    email: string
    oid: string
  }
}