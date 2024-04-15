import React, { useEffect, useState } from 'react';
import { Select } from 'antd';
import {ApiRoutes} from "../../../@types/requests.d";
import  apiCall  from "../../../util/apiFetch";

const { Option } = Select;


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
    courseId: string;
    onSelect: (value: number) => void;
}

const GroupClusterDropdown: React.FC<GroupClusterDropdownProps> = ({ courseId, onSelect }) => {
    const [clusters, setClusters] = useState<Cluster[]>([]); // Gebruik Cluster-interface
    const [loading, setLoading] = useState(false);

    useEffect(() => {
        const fetchClusters = async () => {
            setLoading(true);
            try {
                const response = await apiCall.get(ApiRoutes.COURSE_CLUSTERS, { id: courseId });
                setClusters(response.data); // Zorg ervoor dat de nieuwe staat correct wordt doorgegeven
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
            loading={loading}
            onChange={onSelect}
        >
            {clusters.map((cluster) => (
                <Option key={cluster.clusterId} value={cluster.clusterId}>
                    {cluster.name}
                </Option>
            ))}
        </Select>
    );
};

export default GroupClusterDropdown;
