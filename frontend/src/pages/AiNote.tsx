import React, { useState } from "react";
import { Card, Steps, Button, Upload, Input, message, Spin, Tag, Tabs } from "antd";
import { UploadOutlined, AudioOutlined, FileTextOutlined, NodeIndexOutlined, QuestionCircleOutlined, SaveOutlined } from "@ant-design/icons";
import { aiApi } from "../api/ai";
import { noteApi } from "../api/note";
import { useAuth } from "../context/AuthContext";
import MindMap from "../components/MindMap";

const { TextArea } = Input;

const AiNote: React.FC = () => {
  const { user } = useAuth();
  const [step, setStep] = useState(0);
  const [loading, setLoading] = useState(false);

  // 步骤1：输入文本
  const [inputText, setInputText] = useState("");

  // 步骤2：生成的笔记
  const [noteContent, setNoteContent] = useState("");
  const [noteTitle, setNoteTitle] = useState("");

  // 步骤3：思维导图
  const [mindMapData, setMindMapData] = useState<any>(null);

  // 步骤4：复习题
  const [quizData, setQuizData] = useState<any[]>([]);

  // 语音转文字
  const handleTranscribe = async (file: File) => {
    setLoading(true);
    try {
      const formData = new FormData();
      formData.append("file", file);
      const res: any = await aiApi.transcribe(formData);
      setInputText(res.text || res.data?.text || "");
      message.success("语音转写完成");
    } catch { message.error("语音转写失败"); }
    finally { setLoading(false); }
    return false; // 阻止 Antd 自动上传
  };

  // 生成笔记
  const handleGenerate = async () => {
    if (!inputText.trim()) { message.warning("请输入或上传课堂内容"); return; }
    setLoading(true);
    try {
      const res: any = await aiApi.generateNote({ text: inputText, title: noteTitle || "课堂笔记" });
      setNoteContent(res.data?.content || res.content || "");
      setNoteTitle(res.data?.title || res.title || "课堂笔记");
      setStep(1);
      message.success("笔记生成完成");
    } catch { message.error("笔记生成失败"); }
    finally { setLoading(false); }
  };

  // 生成思维导图
  const handleMindMap = async () => {
    if (!noteContent) { message.warning("请先生成笔记"); return; }
    setLoading(true);
    try {
      const res: any = await aiApi.generateMindMap({ content: noteContent, title: noteTitle });
      setMindMapData(res.data?.mindmap || res.mindmap || null);
      setStep(2);
      message.success("思维导图生成完成");
    } catch { message.error("思维导图生成失败"); }
    finally { setLoading(false); }
  };

  // 生成复习题
  const handleQuiz = async () => {
    if (!noteContent) { message.warning("请先生成笔记"); return; }
    setLoading(true);
    try {
      const res: any = await aiApi.generateQuiz({ content: noteContent, numQuestions: 5 });
      setQuizData(res.data?.questions || res.questions || []);
      setStep(3);
      message.success("复习题生成完成");
    } catch { message.error("复习题生成失败"); }
    finally { setLoading(false); }
  };

  // 保存笔记
  const handleSave = async () => {
    if (!user) return;
    try {
      await noteApi.create({
        title: noteTitle,
        content: noteContent,
        mindMap: mindMapData ? JSON.stringify(mindMapData) : null,
        quizQuestions: quizData.length ? JSON.stringify(quizData) : null,
        userId: user.id,
        source: "AI_GENERATED",
      });
      message.success("笔记已保存");
    } catch { message.error("保存失败"); }
  };

  const tabItems = [
    {
      key: "content",
      label: <span><FileTextOutlined /> 笔记内容</span>,
      children: (
        <TextArea value={noteContent} onChange={(e) => setNoteContent(e.target.value)} rows={15} style={{ fontFamily: "monospace" }} />
      )
    },
    {
      key: "mindmap",
      label: <span><NodeIndexOutlined /> 思维导图</span>,
      children: mindMapData ? <MindMap data={mindMapData} /> : <div style={{ textAlign: "center", padding: 40, color: "#999" }}>点击上方按钮生成思维导图</div>
    },
    {
      key: "quiz",
      label: <span><QuestionCircleOutlined /> 复习题 ({quizData.length})</span>,
      children: quizData.length > 0 ? (
        <div>
          {quizData.map((q: any, i: number) => (
            <Card key={i} size="small" style={{ marginBottom: 8 }}>
              <div style={{ fontWeight: "bold" }}>{i + 1}. {q.question} <Tag>{q.type}</Tag></div>
              <div style={{ color: "#666", marginTop: 4 }}>答案：{q.answer}</div>
            </Card>
          ))}
        </div>
      ) : <div style={{ textAlign: "center", padding: 40, color: "#999" }}>点击上方按钮生成复习题</div>
    }
  ];

  return (
    <Spin spinning={loading}>
      <Steps current={step} style={{ marginBottom: 24 }} items={[
        { title: "输入内容" },
        { title: "生成笔记" },
        { title: "思维导图" },
        { title: "复习题" },
      ]} />

      {/* 输入区 */}
      <Card title="课堂内容输入" style={{ marginBottom: 16 }}>
        <Upload accept="audio/*,video/*" showUploadList={false} beforeUpload={handleTranscribe}>
          <Button icon={<UploadOutlined />} style={{ marginBottom: 8 }}>上传录音文件（自动转写）</Button>
        </Upload>
        <TextArea
          value={inputText}
          onChange={(e) => setInputText(e.target.value)}
          placeholder="或直接粘贴课堂内容..."
          rows={6}
        />
        <div style={{ marginTop: 8, display: "flex", gap: 8 }}>
          <Input placeholder="笔记标题（选填）" value={noteTitle} onChange={(e) => setNoteTitle(e.target.value)} style={{ width: 300 }} />
          <Button type="primary" icon={<FileTextOutlined />} onClick={handleGenerate}>生成笔记</Button>
          <Button icon={<NodeIndexOutlined />} onClick={handleMindMap} disabled={!noteContent}>生成思维导图</Button>
          <Button icon={<QuestionCircleOutlined />} onClick={handleQuiz} disabled={!noteContent}>生成复习题</Button>
          <Button icon={<SaveOutlined />} onClick={handleSave} disabled={!noteContent}>保存笔记</Button>
        </div>
      </Card>

      {/* 结果展示 */}
      {noteContent && (
        <Card title="生成结果">
          <Tabs items={tabItems} />
        </Card>
      )}
    </Spin>
  );
};

export default AiNote;
