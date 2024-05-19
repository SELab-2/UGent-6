import {createContext, Dispatch, FC, PropsWithChildren, useEffect, useState} from "react"
import {LoginStatus} from "../@types/appTypes";
import {useNavigate} from "react-router-dom";
import axios from "axios";
import { AxiosRequestConfig } from "axios"

type Account = {
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
    const [loginStatus, setLoginStatus] = useState<LoginStatus>(LoginStatus.LOGGED_OUT)
    const [account, setAccount] = useState<Account | null>(null)

    useEffect(() => {
        updateAccount()
    }, []);

    const updateAccount = async () => {
        try {
            const res = await axios.get(
                'http://localhost:3000/web/users/isAuthenticated',
                {withCredentials:true } as AxiosRequestConfig
            )
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

    const login = async () => {
        setLoginStatus(LoginStatus.LOGIN_IN_PROGRESS)
    }

    const logout = async () => {
        setIsAuthenticated(false)
        setLoginStatus(LoginStatus.LOGOUT_IN_PROGRESS)
    }

    return <AuthContext.Provider value={{ isAuthenticated, loginStatus, account, updateAccount, login, logout }}>{children}</AuthContext.Provider>
}


export {AuthContext, AuthProvider}