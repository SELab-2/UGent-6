import React from 'react';
import { Space, Tooltip } from 'antd';
import { QuestionCircleOutlined } from '@ant-design/icons';
import MarkdownTextfield from '../input/MarkdownTextfield';

interface CustomTooltipProps {
    label: string;
    tooltipContent: string;
    placement?: 'top' | 'left' | 'right' | 'bottom' | 'topLeft' | 'topRight' | 'bottomLeft' | 'bottomRight' | 'leftTop' | 'leftBottom' | 'rightTop' | 'rightBottom';
}

const CustomTooltip: React.FC<CustomTooltipProps> = ({ label, tooltipContent, placement = 'bottom' }) => {

    const contentLength = tooltipContent.length;
    const calculatedWidth = contentLength > 100 ? "500px" : "auto";
    
    const overlayInnerStyle = { width: calculatedWidth, maxWidth: "75vw", paddingLeft:"12px"}; 

    return (
        <Space>
            {label}
            
            <Tooltip placement={placement} title={<MarkdownTextfield content={tooltipContent} inTooltip={true} />} overlayInnerStyle={overlayInnerStyle} className='tooltip-markdown'>
                <QuestionCircleOutlined style={{ color: 'gray' }} />
            </Tooltip>
        </Space>
    );
};

export default CustomTooltip;
