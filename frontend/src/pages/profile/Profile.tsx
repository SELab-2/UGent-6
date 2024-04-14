import { useEffect, useState } from "react";

import {  useMsal } from "@azure/msal-react";
import { InteractionStatus,  InteractionRequiredAuthError, AccountInfo } from "@azure/msal-browser";
import { loginRequest } from "../../auth/AuthConfig";
import { callMsGraph } from "../../auth/MsGraphApiCall";
import { Spin } from "antd";
import ProfileCard from "./components/ProfileCard"
import useUser from "../../hooks/useUser";



const ProfileContent = () => {
    const { instance, inProgress } = useMsal();
    const [id, setId] = useState<String | null>(null);
    const { user } = useUser()

    useEffect(() => {
        if (!id && inProgress === InteractionStatus.None) {
            callMsGraph().then(response => {
                    if (response) {
                        setId(response.id);
                    } else {
                        throw("User not found");
                    }
                }).catch((e) => {
                if (e instanceof InteractionRequiredAuthError) {

                    instance.acquireTokenRedirect({
                        ...loginRequest,
                        account: instance.getActiveAccount() as AccountInfo
                    });
                }
            }).catch(err => {
                console.log(err);
            });
        }
    }, [inProgress, id, instance]);

    if (user === null) {
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
            <ProfileCard user={user} />
        </div>
    );
};

export function Profile() {
 
    return (
       
            <ProfileContent />
      )
};

export default Profile;