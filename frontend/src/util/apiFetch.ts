import { ApiRoutes, DELETE_Requests, GET_Responses, POST_Requests, POST_Responses, PUT_Requests, PUT_Responses } from "../@types/requests"


import axios, { AxiosError, AxiosResponse, RawAxiosRequestHeaders } from "axios"

import { AxiosRequestConfig } from "axios"



const serverHost =  window.location.origin.includes("localhost") ? "http://localhost:3000" : window.location.origin


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

  if (pathValues) {
    Object.entries(pathValues).forEach(([key, value]) => {
      route = route.replace(":" + key, value.toString())
    })
  }



  const defaultHeaders = {
    "Content-Type": body instanceof FormData ? undefined : "application/json",
  } as RawAxiosRequestHeaders

  const finalHeaders = headers ? { ...defaultHeaders, ...headers } : defaultHeaders

  const url = new URL(route, serverHost)

  const finalConfig: AxiosRequestConfig = {
    method: method,
    url: url.toString(),
    withCredentials:true,
    headers: finalHeaders,
    data: body instanceof FormData ? body : JSON.stringify(body),
    ...config, // spread the config object to merge it with the existing configuration

  }
  return axios(finalConfig)
}

const apiCall = {
  get: async <T extends keyof GET_Responses>(route: T, pathValues?: ApiCallPathValues, headers?: RawAxiosRequestHeaders, config?: AxiosRequestConfig) => apiFetch("GET", route, undefined, pathValues, headers, config) as Promise<AxiosResponse<GET_Responses[T]>>,
  post: async <T extends keyof POST_Requests>(route: T, body: POST_Requests[T] | FormData, pathValues?: ApiCallPathValues, headers?: RawAxiosRequestHeaders) => apiFetch("POST", route, body, pathValues, headers) as Promise<AxiosResponse<POST_Responses[T]>>,
  put: async <T extends keyof PUT_Requests>(route: T, body: PUT_Requests[T], pathValues?: ApiCallPathValues, headers?: RawAxiosRequestHeaders) => apiFetch("PUT", route, body, pathValues, headers) as Promise<AxiosResponse<PUT_Responses[T]>>,
  delete: async <T extends keyof DELETE_Requests>(route: T, body: DELETE_Requests[T], pathValues?: ApiCallPathValues, headers?: RawAxiosRequestHeaders) => apiFetch("DELETE", route, body, pathValues, headers),
  patch: async <T extends keyof PUT_Requests>(route: T, body: Partial<PUT_Requests[T]>, pathValues?: ApiCallPathValues, headers?: RawAxiosRequestHeaders) => apiFetch("PATCH", route, body, pathValues, headers) as Promise<AxiosResponse<PUT_Responses[T]>>,
}


export default apiCall
