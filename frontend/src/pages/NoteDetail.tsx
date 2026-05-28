import React, { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { Button, Input, Tag, message, Spin, Divider, Tabs } from "antd";
import { SaveOutlined, ShareAltOutlined, ArrowLeftOutlined } from "@ant-design/icons";
import { noteApi } from "../api/note";
import MindMap from "../components/MindMap";

const { TextArea } = Input;

const NoteDetail: React.FC = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const [note, setNote] = useState<any>(null);
  const [loading, setLoading] = useState(true);
  const [editContent, setEditContent] = useState("");
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    if (!id) return;
    noteApi.getById(Number(id))
      .then((res: any) => {
        setNote(res.data);
        setEditContent(res.data.content || "");
      })
      .catch(() => message.error("加载失败"))
      .finally(() => setLoading(false));
  }, [id]);

  const handleSave = async () => {
    if (!id) return;
    setSaving(true);
    try {
      await noteApi.update(Number(id), { content: editContent });
      message.success("保存成功");
    } catch { message.error("保存失败"); }
    finally { setSaving(false); }
  };

  const handleShare = async () => {
    if (!id) return;
    try {
      const res: any = await noteApi.share(Number(id));
      message.success(`分享码: ${res.data.shareCode}`);
    } catch { message.error("分享失败"); }
  };

  if (loading) return <Spin size="large" style={{ display: "block", margin: "100px auto" }} />;
  if (!note) return <div>笔记不存在</div>;

  // 解析思维导图JSON
  let mindMapData = null;
  try { mindMapData = note.mindMap ? JSON.parse(note.mindMap) : null; } catch {}

  // 解析复习题JSON
  let quizData = null;
  try { quizData = note.quizQuestions ? JSON.parse(note.quizQuestions) : null; } catch {}

  const tabItems = [
    {
      key: "content",
      label: "笔记内容",
      children: (
        <div>
          <div style={{ marginBottom: 16 }}>
            <Button icon={<ArrowLeftOutlined />} onClick={() => navigate("/notes")} style={{ marginRight: 8 }}>返回</Button>
            <Button type="primary" icon={<SaveOutlined />} onClick={handleSave} loading={saving} style={{ marginRight: 8 }}>保存</Button>
            <Button icon={<ShareAltOutlined />} onClick={handleShare}>分享</Button>
          </div>
          <TextArea
            value={editContent}
            onChange={(e) => setEditContent(e.target.value)}
            rows={20}
            style={{ fontFamily: "monospace", fontSize: 14 }}
          />
        </div>
      )
    },
    {
      key: "mindmap",
      label: "思维导图",
      children: mindMapData ? <MindMap data={mindMapData} /> : <div style={{ textAlign: "center", padding: 40, color: "#999" }}>暂无思维导图数据</div>
    },
    {
      key: "quiz",
      label: `复习题 (${quizData?.length || 0})`,
      children: quizData ? (
        <div>
          {quizData.map((q: any, i: number) => (
            <div key={i} style={{ marginBottom: 16, padding: 16, background: "#fafafa", borderRadius: 8 }}>
              <div style={{ fontWeight: "bold", marginBottom: 8 }}>
                {i + 1}. {q.question}
                <Tag style={{ marginLeft: 8 }} color="blue">{q.type}</Tag>
                <Tag color={q.difficulty === "hard" ? "red" : q.difficulty === "medium" ? "orange" : "green"}>{q.difficulty}</Tag>
              </div>
              <div style={{ color: "#666" }}>答案：{q.answer}</div>
            </div>
          ))}
        </div>
      ) : <div style={{ textAlign: "center", padding: 40, color: "#999" }}>暂无复习题</div>
    }
  ];

  return (
    <div>
      <h2>{note.title} <Tag color={note.source === "AI_GENERATED" ? "blue" : "default"}>{note.source === "AI_GENERATED" ? "AI生成" : "手动"}</Tag></h2>
      <Divider />
      <Tabs items={tabItems} />
    </div>
  );
};

export default NoteDetail;
