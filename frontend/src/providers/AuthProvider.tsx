import {createContext, FC, PropsWithChildren, useEffect, useState} from "react"
import {LoginStatus} from "../@types/appTypes";
import apiCall from "../util/apiFetch";
import {ApiRoutes} from "../@types/requests.d";

/**
 * Context provider that contains the authentication state and account name for in the nav bar.
 */



export type Account = {
    name: string
}

export type AuthContextProps = {
    isAuthenticated: Boolean,
    loginStatus: LoginStatus,
    account: Account | null,
    updateAccount: () => void,
    login: () => void,
    logout: () => void,
}

const AuthContext = createContext({} as AuthContextProps)

const AuthProvider : FC<PropsWithChildren> = ({children}) => {
    const [isAuthenticated, setIsAuthenticated] = useState(false)
    const [loginStatus, setLoginStatus] = useState<LoginStatus>(LoginStatus.LOGIN_IN_PROGRESS)
    const [account, setAccount] = useState<Account | null>(null)

    useEffect(() => {
        updateAccount()
    }, []);

    /**
     * Function that contacts the backend for information on the current authentication state.
     * Stores the result in the state.
     */
    const updateAccount = async () => {
        try {
            const res = await apiCall.get(ApiRoutes.AUTH_INFO)
            if (res.data.isAuthenticated) {
                setIsAuthenticated(true)
                setLoginStatus(LoginStatus.LOGGED_IN)
                setAccount(res.data.account)
            } else {
                setIsAuthenticated(false)
                setLoginStatus(LoginStatus.LOGGED_OUT)
                setAccount(null)
            }
        } catch (err) {
            console.log(err)
        }
    }

    /**
     * Function that updates the login state.
     * Should be used when logging in.
     */
    const login = async () => {
        setLoginStatus(LoginStatus.LOGIN_IN_PROGRESS)
    }

    /**
     * Function that updates the login state.
     * Should be used when logging out.
     */
    const logout = async () => {
        setIsAuthenticated(false)
        setLoginStatus(LoginStatus.LOGOUT_IN_PROGRESS)
    }

    return <AuthContext.Provider value={{ isAuthenticated, loginStatus, account, updateAccount, login, logout }}>{children}</AuthContext.Provider>
}


export {AuthContext, AuthProvider}