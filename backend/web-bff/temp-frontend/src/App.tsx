import {useEffect, useState} from 'react'
import axios from 'axios'
import {Link} from "react-router-dom"

import './App.css'


function App() {

    const [auth, setAuth]
        = useState<{ isAuthenticated:boolean, account: { name:string } | null} | null>(null)

    useEffect(() => {
        axios.get('http://localhost:3000/web/users/isAuthenticated', {withCredentials: true}).then(({data}) => {
            console.log(data)
            setAuth(data);
        })
    })

    if (auth === null) {
        return (
            <>
                <h1>Loading...</h1>
            </>
        )

    } else if (auth.isAuthenticated) {
        return (
            <>
                <h1>Logged in!</h1>
                <p> You are logged in as {auth && auth.account?.name ? auth.account.name : null}</p>
            </>
        )
    } else {
        return (
            <>
                <h1>Welcome, please login</h1>
                <Link to='http://localhost:3000/web/auth/signin'>Login</Link>
            </>
        )
    }


}

export default App
