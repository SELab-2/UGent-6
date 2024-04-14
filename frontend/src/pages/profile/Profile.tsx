import { useEffect, useState } from "react";

import {  useMsal } from "@azure/msal-react";
import { InteractionStatus,  InteractionRequiredAuthError, AccountInfo } from "@azure/msal-browser";
import { loginRequest } from "../../auth/AuthConfig";
import { callMsGraph } from "../../auth/MsGraphApiCall";
import { Spin } from "antd";
import ProfileCard from "./components/ProfileCard"
import useUser from "../../hooks/useUser";
import { User } from "../../providers/UserProvider";

const ProfileContent = () => {
    const { user } = useUser()
    const [tmpUser, setTmpUser] = useState<User | null>(null);

    useEffect(() => {
        setTmpUser({
            courseUrl: "tmp",
            projects_url: "tmp",
            url: "tmp",
            role: "admin",
            email: "tmp@tmp.tmp",
            id: 1,
            name: "Floris",
            surname: "Kornelis van Dijken",
        });
    }, []);

    if (tmpUser === null) {
        return (
          <div style={{ width: "100%", height: "100%", display: "flex", justifyContent: "center", alignItems: "center" }}>
            <Spin
              tip="Loading..."
              size="large"
            />
          </div>
        )
    }

    return (
        <div style={{padding: "3rem"}}>
            <ProfileCard user={tmpUser} />
        </div>
    );
};

export function Profile() {
 
    return (
       
            <ProfileContent />
      )
};

export default Profile;