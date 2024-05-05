import { Select } from "antd";
import { SelectProps } from "antd/lib";
import { FC } from "react";


const AcademicYearSelect: FC<SelectProps<number>> = (props) => {
  const currentYear = new Date().getFullYear();
  const years = Array.from({ length: 5 }, (_, i) => currentYear + i); // Generate next 10 years


  const items: SelectProps<number>["options"] = years.map((year) => ({
    
    label: `${year-1} - ${year}`,
    value: year-1,
  }))


  return (
    <Select {...props} options={items}>
    
    </Select>
  );
};

export default AcademicYearSelect;