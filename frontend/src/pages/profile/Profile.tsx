import { Spin } from "antd";
import ProfileCard from "./components/ProfileCard"
import useUser from "../../hooks/useUser";

const ProfileContent = () => {
    const { user } = useUser()

    if (user === null) {
        return (
          <div style={{ width: "100%", height: "100%", display: "flex", justifyContent: "center", alignItems: "center" }}>
            <Spin
              tip="Loading..."
              size="large"
            ><span> </span></Spin>
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