import { FC, PropsWithChildren, createContext, useState } from "react"
import Error from "../pages/error/Error"

type ErrorMessage = { status: number; message: string } | null
export type ErrorContextT = {
  error: ErrorMessage
  setError: (e: ErrorMessage) => void
}

export const ErrorContext = createContext<ErrorContextT>({} as ErrorContextT)

const ErrorProvider: FC<PropsWithChildren> = ({ children }) => {
  const [error, setError] = useState<ErrorMessage>(null)

  return (
    <ErrorContext.Provider value={{ error, setError }}>
      {error ? (
        <Error
          errorCode={error.status}
          errorMessage={error.message}
        />
      ) : (
        <>{children}</>
      )}
    </ErrorContext.Provider>
  )
}

export default ErrorProvider
