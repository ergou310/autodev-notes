import React, { useEffect, useState } from "react";
import { Table, Button, Tag, Space, Input, message, Popconfirm } from "antd";
import { PlusOutlined, SearchOutlined, ShareAltOutlined, DeleteOutlined, EditOutlined } from "@ant-design/icons";
import { useNavigate } from "react-router-dom";
import { noteApi } from "../api/note";
import { useAuth } from "../context/AuthContext";

const NoteList: React.FC = () => {
  const [notes, setNotes] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [keyword, setKeyword] = useState("");
  const navigate = useNavigate();
  const { user } = useAuth();

  const loadNotes = async () => {
    if (!user) return;
    setLoading(true);
    try {
      const res: any = await noteApi.getByUser(user.id);
      setNotes(res.data || []);
    } catch {
      message.error("加载笔记失败");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { loadNotes(); }, [user]);

  const handleSearch = async () => {
    if (!keyword.trim() || !user) { loadNotes(); return; }
    setLoading(true);
    try {
      const res: any = await noteApi.search(user.id, keyword);
      setNotes(res.data || []);
    } catch { message.error("搜索失败"); }
    finally { setLoading(false); }
  };

  const handleShare = async (id: number) => {
    try {
      const res: any = await noteApi.share(id);
      message.success(`分享码: ${res.data.shareCode}`);
      loadNotes();
    } catch { message.error("分享失败"); }
  };

  const handleDelete = async (id: number) => {
    try {
      await noteApi.delete(id);
      message.success("已删除");
      loadNotes();
    } catch { message.error("删除失败"); }
  };

  const columns = [
    { title: "标题", dataIndex: "title", key: "title",
      render: (text: string, record: any) => (
        <a onClick={() => navigate(`/notes/${record.id}`)}>{text}</a>
      )
    },
    { title: "来源", dataIndex: "source", key: "source",
      render: (s: string) => (
        <Tag color={s === "AI_GENERATED" ? "blue" : "default"}>
          {s === "AI_GENERATED" ? "AI生成" : s === "UPLOAD" ? "上传" : "手动"}
        </Tag>
      )
    },
    { title: "状态", dataIndex: "status", key: "status",
      render: (s: string) => (
        <Tag color={s === "PUBLISHED" ? "green" : s === "ARCHIVED" ? "orange" : "default"}>
          {s === "PUBLISHED" ? "已发布" : s === "ARCHIVED" ? "已归档" : "草稿"}
        </Tag>
      )
    },
    { title: "分享", dataIndex: "shared", key: "shared",
      render: (v: boolean) => v ? <Tag color="green">已分享</Tag> : <Tag>未分享</Tag>
    },
    { title: "更新时间", dataIndex: "updatedAt", key: "updatedAt",
      render: (t: string) => t ? new Date(t).toLocaleString() : "-"
    },
    { title: "操作", key: "action",
      render: (_: any, record: any) => (
        <Space>
          <Button size="small" icon={<EditOutlined />} onClick={() => navigate(`/notes/${record.id}`)}>编辑</Button>
          <Button size="small" icon={<ShareAltOutlined />} onClick={() => handleShare(record.id)}>分享</Button>
          <Popconfirm title="确认删除？" onConfirm={() => handleDelete(record.id)}>
            <Button size="small" danger icon={<DeleteOutlined />}>删除</Button>
          </Popconfirm>
        </Space>
      )
    },
  ];

  return (
    <div>
      <div style={{ display: "flex", justifyContent: "space-between", marginBottom: 16 }}>
        <Input.Search placeholder="搜索笔记" value={keyword} onChange={(e) => setKeyword(e.target.value)} onSearch={handleSearch} style={{ width: 300 }} />
        <Button type="primary" icon={<PlusOutlined />} onClick={() => navigate("/ai-note")}>AI生成笔记</Button>
      </div>
      <Table columns={columns} dataSource={notes} rowKey="id" loading={loading} />
    </div>
  );
};

export default NoteList;
