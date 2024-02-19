import { ApiRoutes, POST_Requests, PUT_Requests } from "../types";
import axios from "axios";
import {msalInstance} from "../index";
import { AxiosRequestConfig } from "axios";
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

async function apiFetch<T extends ApiRoutes>(method: "GET" | "POST" | "PUT" | "DELETE", route: T, body?: any): Promise<any> {

  const account = msalInstance.getActiveAccount();

  if (!account) {
    throw Error("No active account found");
  }

  const response = await msalInstance.acquireTokenSilent({
    scopes: ["User.Read"], // replace with your API's scopes
    account: account
  });
  const accessToken = response.accessToken;

  const headers = {
    'Authorization': `Bearer ${accessToken}`,
    'Content-Type': 'application/json'
  };

  const config: AxiosRequestConfig = {
    method: method,
    url: route.toString(),
    headers: headers,
    data: body
  };

  return axios(config);

}

const calls = {
  get: async <T extends ApiRoutes>(route: T) => apiFetch("GET", route),
  post: async <T extends ApiRoutes>(route: T, body: POST_Requests[T]) => apiFetch("POST", route, body),
  put: async <T extends ApiRoutes>(route: T, body: PUT_Requests[T]) => apiFetch("PUT", route, body),
  delete: async <T extends ApiRoutes>(route: T) => apiFetch("DELETE", route) 
}

export default calls;