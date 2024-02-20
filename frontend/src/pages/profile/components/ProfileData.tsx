import { ApiRoutes } from "../../../types";
import apiCall from "../../../util/apiFetch";

export type GraphData = {
  displayName: string,
  jobTitle: string,
  mail: string,
  businessPhones: string[],
  officeLocation: string
};

export const ProfileData: React.FC<{graphData: GraphData}> = ({graphData}) => {
    const handleApiRequest = async () => {
        console.log("Making request...");
        apiCall.get(ApiRoutes.TEST).then(async (response)=> {
            console.log(response.body);
        } )
    }


  return (
      <ul className="profileData">
          <NameListItem name={graphData.displayName} />
          <JobTitleListItem jobTitle={graphData.jobTitle} />
          <MailListItem mail={graphData.mail} />
          <PhoneListItem phone={graphData.businessPhones[0]} />
          <LocationListItem location={graphData.officeLocation} />
          <button onClick={handleApiRequest}>api request test</button>
      </ul>
  );
};

const NameListItem: React.FC<{name: string}> = ({name}) => (
  <li>
      <div>
          <div>
              <span>👤</span>
          </div>
          <div>
              <h3>Name</h3>
              <p>{name}</p>
          </div>
      </div>
  </li>
);

const JobTitleListItem: React.FC<{jobTitle: string}> = ({jobTitle}) => (
  <li>
      <div>
          <div>
              <span>👔</span>
          </div>
          <div>
              <h3>Title</h3>
              <p>{jobTitle}</p>
          </div>
      </div>
  </li>
);

const MailListItem: React.FC<{mail: string}> = ({mail}) => (
  <li>
      <div>
          <div>
              <span>📧</span>
          </div>
          <div>
              <h3>Mail</h3>
              <p>{mail}</p>
          </div>
      </div>
  </li>
);

const PhoneListItem: React.FC<{phone: string}> = ({phone}) => (
  <li>
      <div>
          <div>
              <span>📞</span>
          </div>
          <div>
              <h3>Phone</h3>
              <p>{phone}</p>
          </div>
      </div>
  </li>
);

const LocationListItem: React.FC<{location: string}> = ({location}) => (
  <li>
      <div>
          <div>
              <span>📍</span>
          </div>
          <div>
              <h3>Location</h3>
              <p>{location}</p>
          </div>
      </div>
  </li>
);

