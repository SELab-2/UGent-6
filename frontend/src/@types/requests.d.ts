/**
 * Routes used to make API calls
 */
export enum ApiRoutes {
  COURSES = "api/courses",
  COURSE = "api/courses/:id",
  COURSE_MEMBERS = "api/courses/:id/users",
  COURSE_MEMBER = "api/courses/:id/users/:userId",
  COURSE_PROJECTS = "api/courses/:id/projects",
  COURSE_CLUSTERS = "api/courses/:id/clusters",

  PROJECT = "api/projects/:id",
  PROJECT_TESTS = "api/projects/:id/tests",
  PROJECT_SUBMISSIONS = "api/projects/:id/submissions",
  PROJECT_SCORE = "api/projects/:id/groups/:groupId/score",
  PROJECT_GROUP = "api/projects/:id/groups/:groupId",

  SUBMISSION = "api/submissions/:id",
  SUBMISSION_FILE = "api/submissions/:id/file",

  CLUSTER = "api/clusters/:id",
  CLUSTER_GROUPS = "api/clusters/:id/groups",

  GROUP = "api/groups/:id",
  GROUP_MEMBERS = "api/groups/:id/members", 
  GROUP_MEMBER = "api/groups/:id/members/:userId", 

  TEST = "api/test",
  USER = "api/users/:id",
  USER_AUTH = "api/auth/:azureId",
}

export type Timestamp = string

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
 * the body of the DELETE requests
 */

export type DELETE_Requests = {
  [ApiRoutes.COURSES]: {
    id: string
  }
}


type Teacher = {
  name: string
  surname: string
  url: string
}

type Course = {
  course_url: string
  name: string
}

/**
 * The response you get from the GET request
 */
export type GET_Responses = {
  [ApiRoutes.COURSES]: {
    id: number
    name: string
    url: string
  }[]
  [ApiRoutes.TEST]: {
    name: string
    firstName: string
    lastName: string
    email: string
    oid: string
  }
  [ApiRoutes.PROJECT_SUBMISSIONS]: GET_Responses[ApiRoutes.SUBMISSION][]
  [ApiRoutes.SUBMISSION]: {
    description: string
    id: string
    project_url: string
    submitted_file_url: string
    submitted_time: Timestamp
    title: string
  }
  [ApiRoutes.SUBMISSION_FILE]: FormData
  [ApiRoutes.COURSE_PROJECTS]: GET_Responses[ApiRoutes.PROJECT][]
  [ApiRoutes.PROJECT]: {
    course: string
    deadline: Timestamp
    description: string
    id: number
    name: string
    submission_url: string
    tests_url: string
  }
  [ApiRoutes.PROJECT_TESTS]: {} // ??
  [ApiRoutes.GROUP]: {
    capacity: number
    id: string
    members_amount: number
    members_url: string
    name: string
  }
  [ApiRoutes.CLUSTER_GROUPS]: {
    groupid: number
    name: string
    capacity: number
    groupcluster_url: string
    members: { userid: number; name:string, surname:string, url:string }[]
  }[]
  [ApiRoutes.PROJECT_SCORE]: {
    score: number
  }

  [ApiRoutes.GROUP_MEMBER]: {
    id: string
    name: string
    surname: string
    url: string
  }
  [ApiRoutes.USERS]: GET_Responses[ApiRoutes.GROUP_MEMBER][]
  [ApiRoutes.GROUP_MEMBERS]: GET_Responses[ApiRoutes.GROUP_MEMBER][]

  [ApiRoutes.COURSE_CLUSTERS]: {
    clusterid: number
    name: string
    capacity: number
    course_url: string
    groups: { name: string; group_url: string }[]
  }[]
  
  [ApiRoutes.CLUSTER]: GET_Responses[ApiRoutes.CLUSTER_GROUPS]
  [ApiRoutes.COURSE]: {
    description: string
    id: number
    members_url: string
    name: string
    teachers: Teacher[] // Changed this
  }
  [ApiRoutes.COURSE_MEMBERS]: GET_Responses[ApiRoutes.GROUP_MEMBER]
  [ApiRoutes.USER]: {
    course_url: string
    projects_url: string
    url: string
    role: "teacher" | "student" | "admin"
    email: string
    id: number
    name: string
    surname: string
  }
  [ApiRoutes.USER_AUTH]: GET_Responses[ApiRoutes.USER]
}
