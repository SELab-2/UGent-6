import type {ProjectFormData} from "../pages/projectCreate/components/ProjectCreateService";

/**
 * Routes used to make API calls
 */
export enum ApiRoutes {
  USER_COURSES = "api/courses", 
  COURSES = "api/courses",
  
  COURSE = "api/courses/:courseId",
  COURSE_MEMBERS = "api/courses/:courseId/members",
  COURSE_PROJECTS = "api/courses/:id/projects",
  COURSE_CLUSTERS = "api/courses/:id/clusters",
  COURSE_GRADES = '/api/courses/:id/grades',

  PROJECTS = "api/projects",
  PROJECT = "api/projects/:id",
  PROJECT_CREATE = "api/courses/:courseId/projects",
  PROJECT_TESTS = "api/projects/:id/tests",
  PROJECT_SUBMISSIONS = "api/projects/:id/submissions",
  PROJECT_SCORE = "api/projects/:id/groups/:groupId/score",
  PROJECT_GROUP = "api/projects/:id/groups/:groupId",
  PROJECT_GROUPS = "api/projects/:id/groups",
  PROJECT_GROUP_SUBMISSIONS = "api/projects/:projectId/submissions/:groupId",

  SUBMISSION = "api/submissions/:id",
  SUBMISSION_FILE = "api/submissions/:id/file",
  SUBMISSION_STRUCTURE_FEEDBACK= "/api/submissions/:id/structurefeedback",
  SUBMISSION_DOCKER_FEEDBACK= "/api/submissions/:id/dockerfeedback",

  CLUSTER = "api/clusters/:id",

  GROUP = "api/groups/:id",
  GROUP_MEMBERS = "api/groups/:id/members", 
  GROUP_MEMBER = "api/groups/:id/members/:userId", 
  GROUP_SUBMISSIONS = "api/projects/:id/groups/:id/submissions",

  TEST = "api/test",
  USER = "api/users/:id",
  USERS = "api/users",
  USER_AUTH = "api/user",
}

export type Timestamp = string

/**
 *  the body of the POST requests
 */
export type POST_Requests = {
  [ApiRoutes.COURSES]: {
    name: string
    description:string
  }
  [ApiRoutes.PROJECT_CREATE]:
      ProjectFormData

    [ApiRoutes.GROUP_MEMBERS]: {
      id: number
    }
}

/**
 * The response you get from the POST request
 */
export type POST_Responses = {

  [ApiRoutes.COURSES]: GET_Responses[ApiRoutes.COURSE],
  [ApiRoutes.PROJECT_CREATE]: GET_Responses[ApiRoutes.PROJECT]

}

/**
 *  the body of the DELETE requests
 */
export type DELETE_Requests = {
  [ApiRoutes.COURSE]: undefined
  [ApiRoutes.PROJECT]: undefined
  [ApiRoutes.GROUP_MEMBER]: undefined
}


/**
 * the body of the PUT requests
 */
export type PUT_Requests = {
  [ApiRoutes.COURSE]: POST_Requests[ApiRoutes.COURSE]

}



export type PUT_Responses = {
  [ApiRoutes.COURSE]: GET_Responses[ApiRoutes.COURSE]
}


type CourseTeacher = {
  name: string
  surname: string
  url: string,
}

type Course = {
  courseUrl: string
  name: string
}

export type ProjectStatus = "correct" | "incorrect" | "not started"
export type CourseRelation = "enrolled" | "course_admin" | "creator"
export type UserRole = "student" | "teacher" | "admin"

/**
 * The response you get from the GET request
 */
export type GET_Responses = {

  [ApiRoutes.TEST]: {
    name: string
    firstName: string
    lastName: string
    email: string
    oid: string
  }
  [ApiRoutes.PROJECT_SUBMISSIONS]: {
    feedback: GET_Responses[ApiRoutes.PROJECT_SCORE] | null, 
    group: GET_Responses[ApiRoutes.GROUP], 
    submission:  GET_Responses[ApiRoutes.SUBMISSION] | null // null if no submission yet
  }[],
  [ApiRoutes.PROJECT_GROUP_SUBMISSIONS]: GET_Responses[ApiRoutes.SUBMISSION][]
  [ApiRoutes.GROUP_SUBMISSIONS]: GET_Responses[ApiRoutes.SUBMISSION]
  [ApiRoutes.SUBMISSION]: {
    submissionId: number
    projectId: number
    groupId: number
    structureAccepted: boolean
    dockerAccepted: boolean
    submissionTime: Timestamp
    projectUrl: ApiRoutes.PROJECT
    groupUrl: ApiRoutes.GROUP
    fileUrl: ApiRoutes.SUBMISSION_FILE
    structureFeedbackUrl: ApiRoutes.SUBMISSION_STRUCTURE_FEEDBACK
    dockerFeedbackUrl: ApiRoutes.SUBMISSION_DOCKER_FEEDBACK
  }
  [ApiRoutes.SUBMISSION_FILE]: BlobPart
  [ApiRoutes.COURSE_PROJECTS]: GET_Responses[ApiRoutes.PROJECT][]
  [ApiRoutes.PROJECT]: {
    course: {
      name: string
      url: string
      courseId: number
    }
    deadline: Timestamp
    description: string
    clusterId: number | null;
    projectId: number
    name: string
    submissionUrl: ApiRoutes.PROJECT_GROUP_SUBMISSIONS
    testsUrl: string
    maxScore:number
    visible: boolean
    status?: ProjectStatus
    progress: {
      completed: number
      total: number
    },
    groupId: number | null //  null if not in a group
  }
  [ApiRoutes.PROJECT_TESTS]: {} // ??
  [ApiRoutes.GROUP]: {
    groupId: number,
    capacity: number,
    name: string
    groupClusterUrl: ApiRoutes.CLUSTER
    members: GET_Responses[ApiRoutes.GROUP_MEMBER][]
  }
  [ApiRoutes.PROJECT_SCORE]: {
    score: number | null, 
    feedback:string | null,
    projectId: number,
    groupId: number
  }, 
  [ApiRoutes.GROUP_MEMBER]: {
    email: string
    name: string
    userId: number
  }
  [ApiRoutes.USERS]: {
    name: string
    userId: number
    url: string
    email: string
    role: UserRole
  }
  [ApiRoutes.GROUP_MEMBERS]: GET_Responses[ApiRoutes.GROUP_MEMBER][]

  [ApiRoutes.COURSE_CLUSTERS]: GET_Responses[ApiRoutes.CLUSTER][]
  
  [ApiRoutes.CLUSTER]: {
    clusterId: number;
    name: string;
    capacity: number;
    groupCount: number;
    created_at: Timestamp;
    groups: GET_Responses[ApiRoutes.GROUP][]
    courseUrl: ApiRoutes.COURSE
  }
  [ApiRoutes.COURSE]: {
    description: string
    courseId: number
    memberUrl: ApiRoutes.COURSE_MEMBERS
    name: string
    teacher: CourseTeacher
    assistents: CourseTeacher[]
    joinUrl: string
    archivedAt: Timestamp | null // null if not archived
    createdAt: Timestamp
  }
  [ApiRoutes.COURSE_MEMBERS]: {
    relation: CourseRelation,
    user: GET_Responses[ApiRoutes.GROUP_MEMBER]
  }[],
  [ApiRoutes.USER]: {
    courseUrl: string
    projects_url: string
    url: string
    role: UserRole
    email: string
    id: number
    name: string
    surname: string
  },
  [ApiRoutes.USER_AUTH]: GET_Responses[ApiRoutes.USER],
  [ApiRoutes.USER_COURSES]: {
    courseId:number, 
    name:string, 
    relation: CourseRelation,
    memberCount: number, 
    archivedAt: Timestamp | null, // null if not archived
    url:string
  }[],
  //[ApiRoutes.PROJECT_GROUP]: GET_Responses[ApiRoutes.CLUSTER_GROUPS][number]
  [ApiRoutes.PROJECT_GROUPS]: GET_Responses[ApiRoutes.GROUP][] //GET_Responses[ApiRoutes.PROJECT_GROUP][]

  [ApiRoutes.PROJECTS]: {
    enrolledProjects: {project: GET_Responses[ApiRoutes.PROJECT], status: ProjectStatus}[],
    adminProjects: Omit<GET_Responses[ApiRoutes.PROJECT], "status">[] 
  },

  [ApiRoutes.COURSE_GRADES]: {
    projectName: string, 
    projectUrl: string,
    projectId: number,
    maxScore: number,
    groupFeedback: GET_Responses[ApiRoutes.PROJECT_SCORE] | null
  }[]

  [ApiRoutes.SUBMISSION_STRUCTURE_FEEDBACK]: string | null  // Null if no feedback is given
  [ApiRoutes.SUBMISSION_DOCKER_FEEDBACK]: string | null // Null if no feedback is given
}
