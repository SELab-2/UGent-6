import {createContext, FC, PropsWithChildren, useEffect, useState} from "react"
import {LoginStatus} from "../@types/appTypes";
import {apiFetch} from "../util/apiFetch";
import {UserContext} from "./UserProvider";


type Account = {
    name: string
}

export type AuthContextProps = {
    isAuthenticated: Boolean,
    loginStatus: LoginStatus,
    account: Account | null,
    updateAccount: () => void
}

const AuthContext = createContext({} as AuthContextProps)

const AuthProvider : FC<PropsWithChildren> = ({children}) => {
    const [isAuthenticated, setIsAuthenticated] = useState(false)
    const [loginStatus, setLoginStatus] = useState<LoginStatus>(LoginStatus.LOGGED_OUT)
    const [account, setAccount] = useState<Account | null>(null)

    useEffect(() => {
        updateAccount()
    }, [loginStatus]);

    const updateAccount = async () => {
        try {
            const res = await apiFetch("GET", "localhost:3000/users/account");
            if (res.data.isAuthenticated) {
                setIsAuthenticated(true)
                setLoginStatus(LoginStatus.LOGGED_IN)
                setAccount(res.data.account)
            }
        } catch (err) {
            console.log(err)
        }
    }
    return <AuthContext.Provider value={{ isAuthenticated, loginStatus, account, updateAccount }}>{children}</AuthContext.Provider>
}

