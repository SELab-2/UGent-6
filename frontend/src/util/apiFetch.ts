import { ApiRoutes, DELETE_Requests, GET_Responses, POST_Requests, POST_Responses, PUT_Requests } from "../@types/requests"
import axios, { AxiosError, AxiosResponse } from "axios"
import { msalInstance } from "../index"
import { AxiosRequestConfig } from "axios"
import { msalConfig } from "../auth/AuthConfig"

const serverHost = "http://localhost:8080" // window.location.origin;
let accessToken: string | null = null
let tokenExpiry: Date | null = null


type ApiCallPathValues = {[param: string]: string | number}
/**
 *
 * @param method
 * @param route
 * @param body
 *
 * @example
 * const courses = await apiFetch("GET", ApiRoutes.COURSES);
 * const newCourse = await apiFetch("POST", ApiRoutes.COURSES, { name: "New Course" });
 *
 */
async function apiFetch(method: "GET" | "POST" | "PUT" | "DELETE" | "PATCH", route: string, body?: any, pathValues?:ApiCallPathValues): Promise<AxiosResponse<any, any>> {
  const account = msalInstance.getActiveAccount()

  if (!account) {
    throw Error("No active account found")
  }

  if(pathValues) {
    Object.entries(pathValues).forEach(([key, value]) => {
      route = route.replace(":"+key, value.toString())
    })
  }

  // check if we have access token
  const now = new Date()

  if (!accessToken || !tokenExpiry || now >= tokenExpiry) {
    const response = await msalInstance.acquireTokenSilent({
      scopes: [msalConfig.auth.clientId + "/.default"],
      account: account,
    })

    accessToken = response.accessToken
    tokenExpiry = response.expiresOn // convert expiry time to JavaScript Date
  }

  const headers = {
    Authorization: `Bearer ${accessToken}`,
    "Content-Type": "application/json",
  }

  const url = new URL(route, serverHost)

  const config: AxiosRequestConfig = {
    method: method,
    url: url.toString(),
    headers: headers,
    data: body,
  }
  

  return axios(config)
}

export type POST_Error<T extends keyof POST_Requests> =  AxiosError<POST_Responses[T], POST_Responses[T]>

const apiCall = {
  get: async <T extends keyof GET_Responses>(route: T, pathValues?:ApiCallPathValues)                                  => apiFetch("GET", route,undefined,pathValues) as Promise<AxiosResponse<GET_Responses[T]>>,
  post: async <T extends keyof POST_Requests>(route: T, body: POST_Requests[T], pathValues?:ApiCallPathValues)         => apiFetch("POST", route, body,pathValues) as Promise<AxiosResponse<POST_Responses[T]>>,
  put: async <T extends keyof PUT_Requests>(route: T, body: PUT_Requests[T], pathValues?:ApiCallPathValues)            => apiFetch("PUT", route, body,pathValues),
  delete: async <T extends keyof DELETE_Requests>(route: T, body: DELETE_Requests[T], pathValues?:ApiCallPathValues)   => apiFetch("DELETE", route, body,pathValues),
  patch: async <T extends keyof PUT_Requests>(route: T, body: Partial<PUT_Requests[T]>, pathValues?:ApiCallPathValues) => apiFetch("PATCH", route, body,pathValues),
}

const apiCallInit = async () => {
  const account = msalInstance.getActiveAccount()

  if (!account) {
    throw Error("No active account found")
  }

  const now = new Date()
  if (!accessToken || !tokenExpiry || now >= tokenExpiry) {
    const response = await msalInstance.acquireTokenSilent({
      scopes: [msalConfig.auth.clientId + "/.default"],
      account: account,
    })

    accessToken = response.accessToken
    tokenExpiry = response.expiresOn // convert expiry time to JavaScript Date
    return accessToken
  } else{
    return accessToken
  }
}

export { accessToken,apiCallInit }

export default apiCall
