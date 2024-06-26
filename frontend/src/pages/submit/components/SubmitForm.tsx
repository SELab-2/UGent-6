import {InboxOutlined} from "@ant-design/icons"
import {Form, FormInstance, Upload} from "antd"
import {FC, useRef, useState} from "react"
import {useTranslation} from "react-i18next"
import {Button} from "antd";
import {Tree} from 'antd';
import {CloseOutlined} from '@ant-design/icons';
import { DataNode } from "antd/es/tree";
import useAppApi from "../../../hooks/useAppApi";

type TreeNode = {
    type: string;
    title: string;
    key: string;
    children: TreeNode[];
};


const SubmitForm: FC<{
    form: FormInstance,
    setFileAdded: (added: boolean) => void,
    onSubmit: (values: any) => void
}> = ({form, setFileAdded, onSubmit}) => {

    const {t} = useTranslation()
    const directoryInputRef = useRef<HTMLInputElement | null>(null);
    const [directoryTree, setDirectoryTree] = useState<TreeNode[]>([]);
    const {message} = useAppApi()

    const normFile = (e: any) => {
        if (Array.isArray(e)) {
            return e
        }
        return e?.fileList
    }

    const onFinish = (values: any) => {
        onSubmit(values);
    };


    const removeEmptyParentNodes = (nodes: TreeNode[]) => {
        for (let i = nodes.length - 1; i >= 0; i--) {
            if (nodes[i].type === 'folder') {
                if (!nodes[i].children || nodes[i].children.length === 0) {
                    nodes.splice(i, 1);
                } else {
                    removeEmptyParentNodes(nodes[i].children);
                }
            }
        }
    };

    const removeNode = (key: string) => {
        const removeNodeRecursive = (nodes: TreeNode[]): boolean => {
            for (let i = 0; i < nodes.length; i++) {
                if (nodes[i].key === key) {
                    nodes.splice(i, 1);
                    return true;
                } else if (nodes[i].children) {
                    const childRemoved = removeNodeRecursive(nodes[i].children);
                    if (childRemoved) {
                        removeEmptyParentNodes(nodes); 
                        return true;
                    }
                }
            }
            return false;
        };

        const newDirectoryTree = [...directoryTree];
        removeNodeRecursive(newDirectoryTree);
        setDirectoryTree(newDirectoryTree);

        
        const newFileList = form.getFieldValue('files').filter((file: any) => !file.uid.startsWith(key));
        form.setFieldsValue({
            files: newFileList
        });

        if (newDirectoryTree.length === 0) {
            setFileAdded(false);
        }
    };
    const markFolders = (nodes: TreeNode[]) => {
        for (let i = 0; i < nodes.length; i++) {
            if (nodes[i].children && nodes[i].children.length > 0) {
                nodes[i].type = 'folder';
                markFolders(nodes[i].children);
            }
        }
    };
    const onDirectoryUpload = (event: React.ChangeEvent<HTMLInputElement>) => {
        const files = event.target.files;
        if (files) {

            // 

            for (let i = 0; i < files.length; i++) {
                const file = files[i];
                const isLt50M = file.size / 1024 / 1024 < 50;
                if (!isLt50M) {
                    message.error(t("project.fileTooLarge"));
                    return;
                }
              }



            const currentFileList = form.getFieldValue('files') || [];
            const newDirectoryTree: TreeNode[] = [...directoryTree]; 

            for (let i = 0; i < files.length; i++) {
                const file = files[i];
                currentFileList.push({
                    uid: file.webkitRelativePath,
                    name: file.name,
                    status: 'done',
                    originFileObj: file
                });

                
                const pathParts = file.webkitRelativePath.split('/');
                let currentNode = newDirectoryTree; 
                for (let j = 0; j < pathParts.length; j++) {
                    let foundNode = currentNode.find(node => node.title === pathParts[j]);
                    if (!foundNode) {
                        foundNode = {
                            title: pathParts[j],
                            key: pathParts.slice(0, j + 1).join('/'),
                            children: [],
                            type: 'file'
                        }; 
                        currentNode.push(foundNode);
                    }
                    currentNode = foundNode.children;
                }
            }
            markFolders(newDirectoryTree);

            form.setFieldsValue({
                files: currentFileList
            });
            setDirectoryTree(newDirectoryTree); 
            setFileAdded(true);
        }
    }
    const renderTreeNodes = (data: TreeNode[]): DataNode[] =>
        data.map((item) => {
            if (item.children?.length) {
                return {
                    title: (
                        <span>
                            {item.title}
                            <Button
                                type="text"
                                size="small"
                                icon={<CloseOutlined/>}
                                onClick={() => removeNode(item.key)}
                                style={{marginLeft: '8px'}}
                            />
                        </span>
                    ),
                    key: item.key,
                    children: renderTreeNodes(item.children),
                    isLeaf:false
                };
            }

            return {
                isLeaf:true,
                title: (
                    <>
                        {item.title}
                        <Button
                            type="text"
                            size="small"
                            icon={<CloseOutlined/>}
                            onClick={() => removeNode(item.key)}
                            style={{marginLeft: '8px'}}
                        />
                    </>
                ),
                key: item.key,
            };
        });

    return (
        <Form form={form} layout="vertical" style={{height: "100%"}} onFinish={onFinish}>

            <Form.Item
                label={t("project.addFiles")}
                name="files"
                valuePropName="fileList"
                getValueFromEvent={normFile}
           
            >
                <Upload.Dragger
                    name="file"
                    beforeUpload={(file) => {
                        const fileSize = file.size / 1024 / 1024;
                        if (fileSize > 50) {
                            message.error(t("project.fileTooLarge"));
                            return Upload.LIST_IGNORE
                        }
                        return false
                    }}
                    multiple={true}
                    style={{height: "100%"}}
                    showUploadList={false}
                    onChange={({file}) => {
                        if (file.status !== 'uploading') {
                            const currentFileList = form.getFieldValue('files') || [];
                            currentFileList.push({
                                uid: file.uid,
                                name: file.name,
                                status: 'done',
                                originFileObj: file
                            });
                            form.setFieldsValue({
                                files: currentFileList
                            });

                            
                            const newDirectoryTree: TreeNode[] = [...directoryTree];
                            newDirectoryTree.push({
                                title: file.name,
                                key: file.uid,
                                children: [],
                                type: ""
                            });
                            setDirectoryTree(newDirectoryTree);

                            setFileAdded(true);
                        }
                    }}
                >
                    <p className="ant-upload-drag-icon">
                        <InboxOutlined/>
                    </p>
                    <p className="ant-upload-text">{t("project.uploadAreaTitle")}</p>
                    <p className="ant-upload-hint">{t("project.uploadAreaSubtitle")}</p>
                </Upload.Dragger>
                </Form.Item>

                <input
                    type="file"
              
                    directory=""
                    webkitdirectory=""
                    mozdirectory=""
                    style={{display: 'none'}}
                    ref={directoryInputRef}
                    onChange={onDirectoryUpload}
                />
                <div style={{overflowY: 'auto', maxHeight: '200px', marginTop: '16px'}}>
                    <Tree.DirectoryTree
                        selectable={false}
                        treeData={renderTreeNodes(directoryTree)}
                    />
                </div>
                <Button
                    type="primary"
                    style={{marginTop: '16px'}}
                    onClick={() => directoryInputRef.current?.click()}
                >
                    {t("project.uploadDirectory")}
                </Button>
        </Form>
    )
}

export default SubmitForm