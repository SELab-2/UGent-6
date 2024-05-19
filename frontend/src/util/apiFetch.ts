import { ApiRoutes, DELETE_Requests, GET_Responses, POST_Requests, POST_Responses, PUT_Requests, PUT_Responses } from "../@types/requests"
import axios, { AxiosError, AxiosResponse } from "axios"
import { AxiosRequestConfig } from "axios"


const serverHost =  window.location.origin.includes("localhost") ? "http://localhost:3000" : window.location.origin
let accessToken: string | null = null
let tokenExpiry: Date | null = null

export type ApiMethods = "GET" | "POST" | "PUT" | "DELETE" | "PATCH" 
export type ApiCallPathValues = {[param: string]: string | number}
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
export async function apiFetch(method: ApiMethods, route: string, body?: any, pathValues?:ApiCallPathValues): Promise<AxiosResponse<any, any>> {

  if(pathValues) {
    Object.entries(pathValues).forEach(([key, value]) => {
      route = route.replace(":"+key, value.toString())
    })
  }


  const headers = {
    "Content-Type": "application/json",
  }

  const url = new URL(route, serverHost)

  const config: AxiosRequestConfig = {
    method: method,
    url: url.toString(),
    headers: headers,
    withCredentials:true,
    data: body,
  }
  

  return axios(config)
}

const apiCall = {
  get: async <T extends keyof GET_Responses>(route: T, pathValues?:ApiCallPathValues)                                  => apiFetch("GET", route,undefined,pathValues) as Promise<AxiosResponse<GET_Responses[T]>>,
  post: async <T extends keyof POST_Requests>(route: T, body: POST_Requests[T], pathValues?:ApiCallPathValues)         => apiFetch("POST", route, body,pathValues) as Promise<AxiosResponse<POST_Responses[T]>>,
  put: async <T extends keyof PUT_Requests>(route: T, body: PUT_Requests[T], pathValues?:ApiCallPathValues)            => apiFetch("PUT", route, body,pathValues) as Promise<AxiosResponse<PUT_Responses[T]>>,
  delete: async <T extends keyof DELETE_Requests>(route: T, body: DELETE_Requests[T], pathValues?:ApiCallPathValues)   => apiFetch("DELETE", route, body,pathValues),
  patch: async <T extends keyof PUT_Requests>(route: T, body: Partial<PUT_Requests[T]>, pathValues?:ApiCallPathValues) => apiFetch("PATCH", route, body,pathValues) as Promise<AxiosResponse<PUT_Responses[T]>>,
}



export default apiCall
