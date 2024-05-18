import { ApiRoutes, DELETE_Requests, GET_Responses, POST_Requests, POST_Responses, PUT_Requests, PUT_Responses } from "../@types/requests"
import axios, { AxiosError, AxiosResponse, RawAxiosRequestHeaders } from "axios"
import { msalInstance } from "../index"
import { AxiosRequestConfig } from "axios"
import { msalConfig } from "../auth/AuthConfig"

const serverHost = window.location.origin.includes("localhost") ? "http://localhost:8080" : window.location.origin
let accessToken: string | null = null
let tokenExpiry: Date | null = null

export type ApiMethods = "GET" | "POST" | "PUT" | "DELETE" | "PATCH"
export type ApiCallPathValues = { [param: string]: string | number }
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
export async function apiFetch(method: ApiMethods, route: string, body?: any, pathValues?: ApiCallPathValues, headers?: RawAxiosRequestHeaders, config?: AxiosRequestConfig): Promise<AxiosResponse<any, any>> {
  const account = msalInstance.getActiveAccount()

  if (!account) {
    throw Error("No active account found")
  }

  if (pathValues) {
    Object.entries(pathValues).forEach(([key, value]) => {
      route = route.replace(":" + key, value.toString())
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

  const defaultHeaders = {
    Authorization: `Bearer ${accessToken}`,
    "Content-Type": body instanceof FormData ? undefined : "application/json",
  }

  const finalHeaders = headers ? { ...defaultHeaders, ...headers } : defaultHeaders

  const url = new URL(route, serverHost)

  const finalConfig: AxiosRequestConfig = {
    method: method,
    url: url.toString(),
    headers: finalHeaders,
    data: body instanceof FormData ? body : JSON.stringify(body),
    ...config, // spread the config object to merge it with the existing configuration
  }
  return axios(finalConfig)
}

const apiCall = {
  get: async <T extends keyof GET_Responses>(route: T, pathValues?: ApiCallPathValues, headers?: { [header: string]: string }, config?: AxiosRequestConfig) => apiFetch("GET", route, undefined, pathValues, headers, config) as Promise<AxiosResponse<GET_Responses[T]>>,
  post: async <T extends keyof POST_Requests>(route: T, body: POST_Requests[T] | FormData, pathValues?: ApiCallPathValues, headers?: { [header: string]: string }) => apiFetch("POST", route, body, pathValues, headers) as Promise<AxiosResponse<POST_Responses[T]>>,
  put: async <T extends keyof PUT_Requests>(route: T, body: PUT_Requests[T], pathValues?: ApiCallPathValues, headers?: { [header: string]: string }) => apiFetch("PUT", route, body, pathValues, headers) as Promise<AxiosResponse<PUT_Responses[T]>>,
  delete: async <T extends keyof DELETE_Requests>(route: T, body: DELETE_Requests[T], pathValues?: ApiCallPathValues, headers?: { [header: string]: string }) => apiFetch("DELETE", route, body, pathValues, headers),
  patch: async <T extends keyof PUT_Requests>(route: T, body: Partial<PUT_Requests[T]>, pathValues?: ApiCallPathValues, headers?: { [header: string]: string }) => apiFetch("PATCH", route, body, pathValues, headers) as Promise<AxiosResponse<PUT_Responses[T]>>,
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
  } else {
    return accessToken
  }
}

export { accessToken, apiCallInit }

export default apiCall
