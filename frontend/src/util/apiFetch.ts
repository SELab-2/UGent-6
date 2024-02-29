import { ApiRoutes, POST_Requests, PUT_Requests } from "../@types/requests";
import axios, { AxiosResponse } from "axios";
import {msalInstance} from "../index";
import { AxiosRequestConfig } from "axios";


const serverHost ="http://localhost:8080" // window.location.origin;
let accessToken: string | null = null;
let tokenExpiry: Date | null = null;

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
async function apiFetch<T extends ApiRoutes>(method: "GET" | "POST" | "PUT" | "DELETE", route: T, body?: any): Promise<AxiosResponse<any, any>>  {

  const account = msalInstance.getActiveAccount();

  if (!account) {
    throw Error("No active account found");
  }

  // check if we have access token
  const now = new Date();

  if (!accessToken || !tokenExpiry || now >= tokenExpiry) {
    const response = await msalInstance.acquireTokenSilent({
      scopes: ["39136cda-f02f-4305-9b08-45f132bab07e/.default"], 
      account: account
    });

    accessToken = response.accessToken;
    tokenExpiry = response.expiresOn  // convert expiry time to JavaScript Date
  }
  

 

  const headers = {
    'Authorization': `Bearer ${accessToken}`,
    'Content-Type': 'application/json'
  };

  const url = new URL(route, serverHost);

  const config: AxiosRequestConfig = {
    method: method,
    url: url.toString(),
    headers: headers,
    data: body
  };
  console.log(config);

  return axios(config);

}

const apiCall = {
  get: async <T extends ApiRoutes>(route: T) => apiFetch("GET", route),
  post: async <T extends keyof POST_Requests>(route: T, body: POST_Requests[T]) => apiFetch("POST", route, body),
  put: async <T extends keyof PUT_Requests>(route: T, body: PUT_Requests[T]) => apiFetch("PUT", route, body),
  delete: async <T extends ApiRoutes>(route: T) => apiFetch("DELETE", route) 
}

export default apiCall;