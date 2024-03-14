import { FC, PropsWithChildren, useState } from "react";
import { createContext } from "vm";




const UserContext = createContext({});

const UserProvider: FC<PropsWithChildren> = ({ children }) => {
  const [user,setUser] = useState<User | null>(null);

  return <UserContext.Provider value={{}}>{children}</UserContext.Provider>
}

export { UserProvider, UserContext }