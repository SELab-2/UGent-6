import {useEffect, useState} from 'react'
import axios from 'axios'

import './App.css'

function App() {
  const [auth, setAuth]
      = useState<{nickname: string, otherKey: number} | null >(null)

  useEffect( () => {
      axios.get('/auth/current-session').then(({data}) => {
          setAuth(data);
      })
  }, [] )

  if (auth === null) {
      return (
            <>
                <h1>Loading...</h1>
            </>
      )
  }  else if (auth) {
      return (
          <>
                <h1>Logged in!</h1>
              <p> You are logged in as {auth && auth.nickname ? auth.nickname : null}</p>
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
