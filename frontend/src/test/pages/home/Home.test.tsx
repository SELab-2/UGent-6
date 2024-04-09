import { render, screen } from "@testing-library/react"
import Home from "../../../pages/index/Home"
import "../../mocks"
import { ApiRoutes, GET_Responses } from "../../../@types/requests"
import { UserCourseType } from "../../../providers/UserProvider"

//TODO: Find better way to write all the mocks

jest.mock("@azure/msal-react",()=> ({
  useAccount: jest.fn(() => ({})),
  MsalAuthenticationTemplate: () => null,
  useMsal: jest.fn(() => ({})),
  MsalAuthenticationResult: () => ({}),
}))

jest.mock("react-syntax-highlighter/dist/esm/styles/prism",()=> ({
  oneDark: {},
  oneLight: {}
}))

jest.mock('react-markdown', () => ({
  Markdown: () => null,
}));

jest.mock("@azure/msal-react", () => ({
  useIsAuthenticated: () => true,
}))


window.matchMedia = window.matchMedia || function() {
  return {
      matches : false,
      addListener : function() {},
      removeListener: function() {}
  };
};


jest.mock("react-i18next", () => ({
  useTranslation: () => ({ t: (key: string) => key }),
}))

jest.mock('react-router-dom', () => ({
  ...jest.requireActual('react-router-dom'), // use actual for all non-hook parts
  useNavigate: () => jest.fn(), // mock function only
}));

jest.mock("../../../hooks/useUser", () => ({
  __esModule: true, // this property makes it work
  default: () => {
    const user: GET_Responses[ApiRoutes.USER] = { courseUrl: "/api/courses", projects_url: "/api/projects/1", url: "/api/users/12", role: "teacher", email: "test@gmail.com", id: 12, name: "Bob", surname: "test" }
    const courses: UserCourseType[] = [{courseId:1,name:"Course 1", relation: "enrolled", url:"/api/courses/1"}]
    return {
      user,
      setUser: () => {},
      courses
    }
  },
}))


test("rendersHome without crashing", () => {
  render(<Home />)
    expect(screen.getByText("home.yourCourses")).toBeInTheDocument()
    expect(screen.getByText("home.yourProjects")).toBeInTheDocument()


  })
  
test("displays courses", () => {
  render(<Home />)
  expect(screen.getByText("home.yourCourses")).toBeInTheDocument()
  expect(screen.getByText("Course 1")).toBeInTheDocument()
})