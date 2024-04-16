import React, { useEffect, useState } from 'react';
import { Select, SelectProps } from 'antd';
import {ApiRoutes} from "../../../@types/requests.d";
import  apiCall  from "../../../util/apiFetch";


interface Cluster {
    clusterId: number;
    name: string;
    capacity: number;
    groupCount: number;
    groups: {
        groupId: number;
        capacity: number;
        name: string;
        groupClusterUrl: ApiRoutes.CLUSTER;
        members: any[]; // Aanpas dit aan het werkelijke type van members
    }[];
    courseUrl: ApiRoutes.COURSE;
}


interface GroupClusterDropdownProps {
    courseId: string|number;
}

const GroupClusterDropdown: React.FC<GroupClusterDropdownProps & SelectProps> = ({ courseId, ...args  }) => {
    const [clusters, setClusters] = useState<SelectProps['options']>([]); // Gebruik Cluster-interface
    const [loading, setLoading] = useState(false);
    

    useEffect(() => {
        const fetchClusters = async () => {
            setLoading(true);
            try {
                const response = await apiCall.get(ApiRoutes.COURSE_CLUSTERS, { id: courseId });
                const options: SelectProps['options'] = response.data.map(
                    (cluster: Cluster) => ({ label: cluster.name, value: cluster.clusterId })
                )

                setClusters(options); // Zorg ervoor dat de nieuwe staat correct wordt doorgegeven
            } catch (error) {
                console.error('Error fetching clusters:', error);
            } finally {
                setLoading(false);
            }
        };

        fetchClusters();
    }, [courseId]);

    return (
        <Select
            {...args}
            loading={loading}
            options={clusters}
       />
    );
};

export default GroupClusterDropdown;
