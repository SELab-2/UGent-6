import { Button, List, Tag } from "antd"
import {FC, useMemo, useState} from "react"
import { UserCourseType } from "../../../providers/UserProvider"
import { Link } from "react-router-dom"
import { AppRoutes } from "../../../@types/routes"
import { useTranslation } from "react-i18next"
import { InboxOutlined, TeamOutlined } from "@ant-design/icons"

const CoursesList: FC<{ courses: UserCourseType[] | null; role?: "enrolled" | "admin" | null; filter?: string }> = ({ courses, role, filter }) => {
    const { t } = useTranslation();
    const [ascendingOrder, setAscendingOrder] = useState(true);

    const filteredList = useMemo(() => {
        if (!courses) return courses;
        return courses.filter((c) => {
            let r = true;
            if (role) {
                if (role === "enrolled") r = role === c.relation;
                else r = c.relation === "course_admin" || c.relation === "creator";
            }

            if (filter) {
                r = r && c.name.toLowerCase().includes(filter.toLowerCase());
            }
            return r;
        });
    }, [courses, role, filter]);

    // Sort the filtered list based on the name of the course
    const sortedList = useMemo(() => {
        if (!filteredList) return filteredList;
        return [...filteredList].sort((a, b) => {
            if (ascendingOrder) {
                return a.name.localeCompare(b.name);
            } else {
                return b.name.localeCompare(a.name);
            }
        });
    }, [filteredList, ascendingOrder]);

    const toggleSortOrder = () => {
        setAscendingOrder((prevOrder) => !prevOrder);
    };

    return (
        <div>
            <Button onClick={toggleSortOrder}>
                {ascendingOrder ? t("courses.sortAscending") : t("courses.sortDescending")}
            </Button>
            <List
                locale={{ emptyText: t("courses.noCourses") }}
                dataSource={sortedList ?? []}
                loading={sortedList === null}
                renderItem={(course) => (
                    <List.Item>
                        <List.Item.Meta
                            title={
                                <Link to={AppRoutes.COURSE.replace(":courseId", course.courseId + "")}>
                                    <Button type="link">{course.name}</Button>
                                </Link>
                            }
                        />
                        <Tag
                            icon={<TeamOutlined />}
                        >
                            {course.memberCount} {t("courses.member" + (course.memberCount === 1 ? "" : "s"))}
                        </Tag>

                        {course.archivedAt && <Tag
                            icon={<InboxOutlined />}
                            color="yellow"
                        >{t("courses.archived")}
                        </Tag>}
                    </List.Item>
                )}
            />
        </div>
    );
};

export default CoursesList;
