import {useEffect, useState} from 'react'
import axios from 'axios'

import './App.css'

type Account = {
    name: string
}

function App() {
    const [isAuth, setIsAuth]
        = useState<boolean | null>(null)
    const [account, setAccount] = useState<Account | null>(null)

    useEffect(() => {
        axios.get("/users/account").then(({data}) => {
            console.log(data)
            setIsAuth(data.isAuthenticated)
            setAccount(data.account)
        })
    }, [isAuth, account])

    if (isAuth === null) {
        return (
            <>
                <h1>Loading...</h1>
            </>
        )
    } else if (isAuth) {
        return (
            <>
                <h1>Logged in!</h1>
                <p> You are logged in as {account && account.name? account.name : null}</p>
            </>
        )
    } else {
        return (
            <>
                <h1>Welcome</h1>
            </>
        )
    }


}

export default App
