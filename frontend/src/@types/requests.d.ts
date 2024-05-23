import type {ProjectFormData} from "../pages/projectCreate/components/ProjectCreateService";
import {Account} from "../providers/AuthProvider";

/**
 * Routes used to make API calls
 */
export enum ApiRoutes {

  AUTH_INFO = "/web/users/isAuthenticated",

  USER_COURSES = "/web/api/courses",
  COURSES = "/web/api/courses",


  COURSE = "/web/api/courses/:courseId",
  COURSE_MEMBERS = "/web/api/courses/:courseId/members",
  COURSE_MEMBER = "/web/api/courses/:courseId/members/:userId",
  COURSE_PROJECTS = "/web/api/courses/:id/projects",
  COURSE_CLUSTERS = "/web/api/courses/:id/clusters",
  COURSE_GRADES = "/web/api/courses/:id/grades",
  COURSE_LEAVE = "/web/api/courses/:courseId/leave",
  COURSE_COPY = "/web/api/courses/:courseId/copy",
  COURSE_JOIN = "/web/api/courses/:courseId/join/:courseKey",
  COURSE_JOIN_WITHOUT_KEY = "/web/api/courses/:courseId/join",
  COURSE_JOIN_LINK = "/web/api/courses/:courseId/joinKey",

  PROJECTS = "/web/api/projects",
  PROJECT = "/web/api/projects/:id",
  PROJECT_CREATE = "/web/api/courses/:courseId/projects",
  PROJECT_TESTS = "/web/api/projects/:id/tests",
  PROJECT_SUBMISSIONS = "/web/api/projects/:id/submissions",
  PROJECT_SCORE = "/web/api/projects/:id/groups/:groupId/score",
  PROJECT_GROUP = "/web/api/projects/:id/groups/:groupId",
  PROJECT_GROUPS = "/web/api/projects/:id/groups",
  PROJECT_GROUP_SUBMISSIONS = "/web/api/projects/:projectId/submissions/:groupId",
  PROJECT_TEST_SUBMISSIONS = "/web/api/projects/:projectId/adminsubmissions",
  PROJECT_TESTS_UPLOAD = "/web/api/projects/:id/tests/extrafiles",
  PROJECT_SUBMIT = "/web/api/projects/:id/submit",
  PROJECT_DOWNLOAD_ALL_SUBMISSIONS = "/web/api/projects/:id/submissions/files",


  SUBMISSION = "/web/api/submissions/:id",
  SUBMISSION_FILE = "/web/api/submissions/:id/file",
  SUBMISSION_STRUCTURE_FEEDBACK= "/web/api/submissions/:id/structurefeedback",
  SUBMISSION_DOCKER_FEEDBACK= "/web/api/submissions/:id/dockerfeedback",
  SUBMISSION_ARTIFACT="/web/api/submissions/:id/artifacts",



  CLUSTER = "/web/api/clusters/:id",
  CLUSTER_FILL = "/web/api/clusters/:id/fill",
  CLUSTER_GROUPS = "/web/api/clusters/:id/groups",

  GROUP = "/web/api/groups/:id",
  GROUP_MEMBERS = "/web/api/groups/:id/members",
  GROUP_MEMBER = "/web/api/groups/:id/members/:userId",
  GROUP_SUBMISSIONS = "/web/api/projects/:id/groups/:id/submissions",

  USER = "/web/api/users/:id",
  USERS = "/web/api/users",
  USER_AUTH = "/web/api/user",
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
  [ApiRoutes.PROJECT_CREATE]: {
    name: string;
    description: string;
    groupClusterId: number;
    testId: number | null;
    visible: boolean;
    maxScore: number;
    deadline: Date | null;
    visibleAfter: Date | null; 
}

    [ApiRoutes.GROUP_MEMBERS]: {
        id: number
    }
    [ApiRoutes.PROJECT_SUBMIT]: {
        file: FormData
    }

  [ApiRoutes.COURSE_CLUSTERS]: {
    name: string
    capacity: number
    groupCount: number
  },
  [ApiRoutes.PROJECT_TESTS]: Omit<GET_Responses[ApiRoutes.PROJECT_TESTS], "projectUrl" | "extraFilesUrl" | "extraFilesName">
  [ApiRoutes.COURSE_COPY]: undefined
  [ApiRoutes.COURSE_JOIN]: undefined
  [ApiRoutes.COURSE_JOIN_WITHOUT_KEY]: undefined
  [ApiRoutes.PROJECT_SCORE]: Omit<GET_Responses[ApiRoutes.PROJECT_SCORE], "groupId" | "projectId">
  [ApiRoutes.CLUSTER_GROUPS]: {name: string}
}

/**
 * The response you get from the POST request
 */
export type POST_Responses = {

  [ApiRoutes.PROJECT_SUBMIT]: GET_Responses[ApiRoutes.SUBMISSION]
  [ApiRoutes.COURSES]: GET_Responses[ApiRoutes.COURSE],
  [ApiRoutes.PROJECT_CREATE]: GET_Responses[ApiRoutes.PROJECT]
  [ApiRoutes.GROUP_MEMBERS]: GET_Responses[ApiRoutes.GROUP_MEMBERS]
  [ApiRoutes.COURSE_CLUSTERS]: GET_Responses[ApiRoutes.CLUSTER],
  [ApiRoutes.PROJECT_TESTS]: GET_Responses[ApiRoutes.PROJECT_TESTS]
  [ApiRoutes.COURSE_COPY]: GET_Responses[ApiRoutes.COURSE]
  [ApiRoutes.COURSE_JOIN]: {name:string, description: string}
  [ApiRoutes.COURSE_JOIN_WITHOUT_KEY]: POST_Responses[ApiRoutes.COURSE_JOIN]
  [ApiRoutes.PROJECT_SCORE]: GET_Responses[ApiRoutes.PROJECT_SCORE]
  [ApiRoutes.CLUSTER_GROUPS]: undefined
}

/**
 *  the body of the DELETE requests
 */
export type DELETE_Requests = {
  [ApiRoutes.COURSE]: undefined
  [ApiRoutes.PROJECT]: undefined
  [ApiRoutes.GROUP_MEMBER]: undefined
  [ApiRoutes.COURSE_LEAVE]: undefined
  [ApiRoutes.COURSE_MEMBER]: undefined
  [ApiRoutes.PROJECT_TESTS]: undefined
  [ApiRoutes.COURSE_JOIN_LINK]: undefined
  [ApiRoutes.PROJECT_TESTS_UPLOAD]: undefined
  [ApiRoutes.CLUSTER]: undefined
}


/**
 * the body of the PUT & PATCH requests
 */
export type PUT_Requests = {
  [ApiRoutes.COURSE]: POST_Requests[ApiRoutes.COURSE]
  [ApiRoutes.PROJECT]: ProjectFormData
  [ApiRoutes.COURSE_MEMBER]: { relation: CourseRelation }
  [ApiRoutes.PROJECT_SCORE]: { score: number | null , feedback: string},
  [ApiRoutes.PROJECT_TESTS]: POST_Requests[ApiRoutes.PROJECT_TESTS]
  [ApiRoutes.USER]: {
    name: string
    surname: string
    email: string
    role: UserRole
  }


  [ApiRoutes.CLUSTER_FILL]: {
    [groupName:string]: number[] /* userId[] */
  }
  [ApiRoutes.COURSE_JOIN_LINK]: undefined
  [ApiRoutes.PROJECT_TESTS_UPLOAD]: FormData
}


export type PUT_Responses = {
  [ApiRoutes.COURSE]: GET_Responses[ApiRoutes.COURSE]
  [ApiRoutes.PROJECT]: GET_Responses[ApiRoutes.PROJECT]
  [ApiRoutes.USER]: GET_Responses[ApiRoutes.USER]
  [ApiRoutes.COURSE_MEMBER]: GET_Responses[ApiRoutes.COURSE_MEMBERS]
  [ApiRoutes.PROJECT_SCORE]: GET_Responses[ApiRoutes.PROJECT_SCORE]
  [ApiRoutes.PROJECT_TESTS]: GET_Responses[ApiRoutes.PROJECT_TESTS]
  [ApiRoutes.CLUSTER_FILL]: PUT_Requests[ApiRoutes.CLUSTER_FILL]
  [ApiRoutes.COURSE_JOIN_LINK]: ApiRoutes.COURSE_JOIN
  [ApiRoutes.PROJECT_TESTS_UPLOAD]: undefined
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

export type DockerStatus = "no_test" | "running" | "finished" | "aborted"
export type ProjectStatus = "correct" | "incorrect" | "not started" | "no group"
export type CourseRelation = "enrolled" | "course_admin" | "creator"
export type UserRole = "student" | "teacher" | "admin"

type SubTest = {
  testName: string, // naam van de test
  testDescription: string, // beschrijving van de test
  correct: string,  // verwachte output
  output: string,  // gegenereerde output
  required: boolean,  //  of de test verplicht is
  //FIXME: typo, moet success zijn ipv succes
  succes: boolean, // of de test geslaagd is
}

type DockerFeedback = {
  type: "SIMPLE",
  feedback: string,  // de logs van de dockerrun
  allowed: boolean // vat samen of de test geslaagd is of niet
} | {
  type: "TEMPLATE",
  feedback: {
    subtests: SubTest[]
  }
  allowed: boolean
} | {
  type: "NONE",
  feedback: "",
  allowed: true
}



/**
 * The response you get from the GET request
 */
export type GET_Responses = {
  [ApiRoutes.PROJECT_SUBMISSIONS]: {
    feedback: GET_Responses[ApiRoutes.PROJECT_SCORE] | null,
    group: GET_Responses[ApiRoutes.GROUP],
    submission:  GET_Responses[ApiRoutes.SUBMISSION] | null // null if no submission yet
  }[],
  [ApiRoutes.PROJECT_GROUP_SUBMISSIONS]: GET_Responses[ApiRoutes.SUBMISSION][]
  [ApiRoutes.PROJECT_TEST_SUBMISSIONS]: GET_Responses[ApiRoutes.PROJECT_GROUP_SUBMISSIONS]
  [ApiRoutes.GROUP_SUBMISSIONS]: GET_Responses[ApiRoutes.SUBMISSION]
  [ApiRoutes.SUBMISSION]: {
    submissionId: number
    projectId: number
    groupId: number | null
    structureAccepted: boolean,
    dockerStatus: DockerStatus,
    submissionTime: Timestamp
    projectUrl: ApiRoutes.PROJECT
    groupUrl: ApiRoutes.GROUP | null
    fileUrl: ApiRoutes.SUBMISSION_FILE
    structureFeedback: string
    dockerFeedback: DockerFeedback,
    artifactUrl: ApiRoutes.SUBMISSION_ARTIFACT | null
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
    maxScore: number | null
    visible: boolean
    visibleAfter?: Timestamp
    status?: ProjectStatus
    progress: {
      completed: number
      total: number
    },
    groupId: number | null //  null if not in a group
  }
  [ApiRoutes.PROJECT_TESTS]: {
    projectUrl: ApiRoutes.PROJECT,
    dockerImage: string | null, 
    dockerScript: string | null,
    dockerTemplate: string | null,
    structureTest: string | null,
    extraFilesUrl: ApiRoutes.PROJECT_TESTS_UPLOAD
    extraFilesName: string
  } 

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
    studentNumber: string | null // Null in case of enrolled/student
  }
  [ApiRoutes.USERS]: {
    name: string
    surname: string
    id: number
    url: string
    email: string
    role: UserRole
  }[]
  [ApiRoutes.GROUP_MEMBERS]: GET_Responses[ApiRoutes.GROUP_MEMBER][]

  [ApiRoutes.COURSE_CLUSTERS]: GET_Responses[ApiRoutes.CLUSTER][]
  
  [ApiRoutes.CLUSTER]: {
    clusterId: number;
    name: string;
    capacity: number;
    groupCount: number;
    createdAt: Timestamp;
    groups: GET_Responses[ApiRoutes.GROUP][]
    courseUrl: ApiRoutes.COURSE,
    lockGroupsAfter: Timestamp | null // means students can't join or leave the group
  }
  [ApiRoutes.COURSE]: {
    description: string
    courseId: number
    memberUrl: ApiRoutes.COURSE_MEMBERS
    name: string
    teacher: CourseTeacher
    assistents: CourseTeacher[]
    joinUrl: ApiRoutes.COURSE_JOIN
    joinKey: string | null
    archivedAt: Timestamp | null // null if not archived
    year: number
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
    year: number // Year of the course
    url:string
  }[],
  //[ApiRoutes.PROJECT_GROUP]: GET_Responses[ApiRoutes.CLUSTER_GROUPS][number]
  [ApiRoutes.PROJECT_GROUPS]: GET_Responses[ApiRoutes.GROUP][] //GET_Responses[ApiRoutes.PROJECT_GROUP][]

    [ApiRoutes.CLUSTER]: {
        clusterId: number;
        name: string;
        capacity: number;
        groupCount: number;
        createdAt: Timestamp;
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
        year: number
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
        studentNumber: string | null // Null in case of enrolled/student
    },
    [ApiRoutes.USER_AUTH]: GET_Responses[ApiRoutes.USER],
    [ApiRoutes.USER_COURSES]: {
        courseId: number,
        name: string,
        relation: CourseRelation,
        memberCount: number,
        archivedAt: Timestamp | null, // null if not archived
        year: number // Year of the course
        url: string
    }[],
    //[ApiRoutes.PROJECT_GROUP]: GET_Responses[ApiRoutes.CLUSTER_GROUPS][number]
    [ApiRoutes.PROJECT_GROUPS]: GET_Responses[ApiRoutes.GROUP][] //GET_Responses[ApiRoutes.PROJECT_GROUP][]

    [ApiRoutes.PROJECTS]: {
        enrolledProjects: { project: GET_Responses[ApiRoutes.PROJECT], status: ProjectStatus }[],
        adminProjects: Omit<GET_Responses[ApiRoutes.PROJECT], "status">[]
    },

  [ApiRoutes.COURSE_GRADES]: {
    projectName: string, 
    projectUrl: string,
    projectId: number,
    maxScore: number | null,
    groupFeedback: GET_Responses[ApiRoutes.PROJECT_SCORE] | null
  }[]

  [ApiRoutes.SUBMISSION_STRUCTURE_FEEDBACK]: string | null  // Null if no feedback is given
  [ApiRoutes.SUBMISSION_DOCKER_FEEDBACK]: string | null // Null if no feedback is given

  [ApiRoutes.SUBMISSION_ARTIFACT]: Blob // returned het artifact als zip

  [ApiRoutes.COURSE_JOIN]: GET_Responses[ApiRoutes.COURSE]
  [ApiRoutes.COURSE_JOIN_WITHOUT_KEY]: GET_Responses[ApiRoutes.COURSE]
  [ApiRoutes.PROJECT_TESTS_UPLOAD]: Blob
  [ApiRoutes.PROJECT_DOWNLOAD_ALL_SUBMISSIONS]: Blob
  [ApiRoutes.AUTH_INFO]: {isAuthenticated:boolean, account: Account|null}
}

