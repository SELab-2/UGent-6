import axios, { AxiosResponse } from "axios"
import useAppApi from "../../../hooks/useAppApi"
import { useTranslation } from "react-i18next"
import { Alert } from "antd"
import { useContext } from "react"
import { ErrorContext } from "../../../providers/ErrorProvider"
import { ApiRoutes, DELETE_Requests, GET_Responses, POST_Requests, POST_Responses, PUT_Requests, PUT_Responses } from "../../../@types/requests"
import { ApiCallPathValues, ApiMethods, apiFetch } from "../../../util/apiFetch"

type FeedbackModes = "message" | "page" | "alert" | "none"
type HandleErrorOptions = {
  mode?: FeedbackModes
  successMessage?: string
  errorMessage?: string
}

type ApiProps = HandleErrorOptions & {
  pathValues?: ApiCallPathValues
  body?: any
}

type HandleErrorReturn<T> =
  | {
      response: AxiosResponse<T, any>
      success: true
    }
  | {
      response: null
      success: false
      alert?: JSX.Element
      errorMessage?: string
    }

const useApi = () => {
  const { message } = useAppApi()
  const { t } = useTranslation()
  const { setError } = useContext(ErrorContext)

  /**
   *
   * @param apiCall the api call
   * @param mode the modes of user feedback
   *  - 'message': uses the useAppApi message to show a message of whether is a success or an error
   *  - 'alert': returns a react component with an alert component that can be displayed in the UI
   *  - 'page': redirect the user to an error page if error is thrown
   *  - 'none': returns the response of the api call, nothing special is done here (default)
   * @returns HandleErrorReturn
   *
   * @example
   *
   *
   */

  const doApiCall = async (method: ApiMethods, route: string, apiOptions: ApiProps, options: HandleErrorOptions | FeedbackModes = "none"): Promise<HandleErrorReturn<any>> => {
    type Ret = HandleErrorReturn<any>
    if (typeof options === "string") options = { mode: options }
    let result: Partial<Ret> = {}

    try {
      const response = await apiFetch(method, route, apiOptions.body, apiOptions.pathValues)
      result.response = response
      result.success = true
      if (options.mode === "message" && options.successMessage) {
        message.success(options.successMessage)
      }
      return result as Ret
    } catch (err) {
      console.error(err)
      result.success = false
      if (result.success !== false) throw err // Yes this check is useless, but it's to make typescript happy
      result.response = null

      let errMessage = options.errorMessage || ""
      let status = 500

      if (axios.isAxiosError(err)) {
        errMessage ||= err.response?.data.message || t("woops")
        status = err.response?.status || 500
      } else {
        errMessage ||= t("woops")
      }

      result.errorMessage = errMessage

      if (options.mode === "alert") {
        result.alert = (
          <Alert
            type="error"
            message={errMessage}
          />
        )
      } else if (options.mode === "message") {
        message.error(errMessage)
      } else if (options.mode === "page") {
        setError({
          status,
          message: errMessage,
        })
      }
    }

    return result as Ret
  }

  return {
    GET: async <T extends keyof GET_Responses>(route: T, o: { pathValues?: ApiCallPathValues }, options?: HandleErrorOptions | FeedbackModes) => doApiCall("GET", route, o, options) as Promise<HandleErrorReturn<GET_Responses[T]>>,
    POST: async <T extends keyof POST_Requests>(route: T, o: { body: POST_Requests[T]; pathValues?: ApiCallPathValues }, options?: HandleErrorOptions | FeedbackModes) => doApiCall("POST", route, o, options) as Promise<HandleErrorReturn<POST_Responses[T]>>,
    PUT: async <T extends keyof PUT_Requests>(route: T, o: { body: PUT_Requests[T]; pathValues?: ApiCallPathValues }, options?: HandleErrorOptions | FeedbackModes) => doApiCall("PUT", route, o, options) as Promise<HandleErrorReturn<PUT_Responses[T]>>,
    DELETE: async <T extends keyof DELETE_Requests>(route: T, o: { body: DELETE_Requests[T]; pathValues?: ApiCallPathValues }, options?: HandleErrorOptions | FeedbackModes) => doApiCall("DELETE", route, o, options),
    PATCH: async <T extends keyof PUT_Requests>(route: T, o: { body: Partial<PUT_Requests[T]>; pathValues?: ApiCallPathValues }, options?: HandleErrorOptions | FeedbackModes) => doApiCall("PATCH", route, o, options) as Promise<HandleErrorReturn<PUT_Responses[T]>>,
  }
}

export default useApi
