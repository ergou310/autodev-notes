import React, { useState } from "react";
import { Card, Input, Button, List, Tag, Avatar, message, Spin } from "antd";
import { SendOutlined, UserOutlined, RobotOutlined, FileTextOutlined } from "@ant-design/icons";
import { aiApi } from "../api/ai";

const QA: React.FC = () => {
  const [question, setQuestion] = useState("");
  const [messages, setMessages] = useState<Array<{ role: string; content: string; sources?: any[] }>>([]);
  const [loading, setLoading] = useState(false);

  const handleAsk = async () => {
    if (!question.trim()) return;
    const q = question;
    setQuestion("");
    setMessages((prev) => [...prev, { role: "user", content: q }]);
    setLoading(true);

    try {
      const res: any = await aiApi.askQuestion({ question: q });
      const data = res.data || res;
      setMessages((prev) => [...prev, {
        role: "assistant",
        content: data.answer || "暂无回答",
        sources: data.sources || [],
      }]);
    } catch {
      setMessages((prev) => [...prev, { role: "assistant", content: "抱歉，回答失败，请稍后重试。" }]);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{ display: "flex", flexDirection: "column", height: "calc(100vh - 200px)" }}>
      <Card title="智能问答" style={{ flex: 1, display: "flex", flexDirection: "column" }}
        bodyStyle={{ flex: 1, display: "flex", flexDirection: "column", overflow: "auto" }}>
        {/* 消息列表 */}
        <div style={{ flex: 1, overflow: "auto", marginBottom: 16, padding: "0 8px" }}>
          {messages.length === 0 && (
            <div style={{ textAlign: "center", color: "#999", padding: 60 }}>
              <RobotOutlined style={{ fontSize: 48, marginBottom: 16 }} />
              <p>你好！我是智能问答助手，请输入你的问题。</p>
              <p style={{ fontSize: 12 }}>提示：请先上传课件到 RAG 系统，以获得基于文档的精准回答。</p>
            </div>
          )}
          {messages.map((msg, i) => (
            <div key={i} style={{ display: "flex", justifyContent: msg.role === "user" ? "flex-end" : "flex-start", marginBottom: 16 }}>
              {msg.role !== "user" && <Avatar icon={<RobotOutlined />} style={{ marginRight: 8, background: "#1890ff" }} />}
              <div style={{
                maxWidth: "70%",
                padding: "12px 16px",
                borderRadius: 12,
                background: msg.role === "user" ? "#1890ff" : "#f5f5f5",
                color: msg.role === "user" ? "#fff" : "#333",
              }}>
                <div style={{ whiteSpace: "pre-wrap" }}>{msg.content}</div>
                {msg.sources && msg.sources.length > 0 && (
                  <div style={{ marginTop: 8, paddingTop: 8, borderTop: "1px solid #e8e8e8" }}>
                    <div style={{ fontSize: 12, color: "#999", marginBottom: 4 }}>参考来源：</div>
                    {msg.sources.map((s: any, j: number) => (
                      <Tag key={j} icon={<FileTextOutlined />} color="blue" style={{ marginBottom: 2 }}>
                        {s.doc_title} (相关度: {(s.score * 100).toFixed(0)}%)
                      </Tag>
                    ))}
                  </div>
                )}
              </div>
              {msg.role === "user" && <Avatar icon={<UserOutlined />} style={{ marginLeft: 8 }} />}
            </div>
          ))}
          {loading && (
            <div style={{ display: "flex", marginBottom: 16 }}>
              <Avatar icon={<RobotOutlined />} style={{ marginRight: 8, background: "#1890ff" }} />
              <div style={{ padding: "12px 16px", borderRadius: 12, background: "#f5f5f5" }}>
                <Spin size="small" /> 思考中...
              </div>
            </div>
          )}
        </div>

        {/* 输入框 */}
        <div style={{ display: "flex", gap: 8 }}>
          <Input
            value={question}
            onChange={(e) => setQuestion(e.target.value)}
            onPressEnter={handleAsk}
            placeholder="输入你的问题..."
            size="large"
          />
          <Button type="primary" icon={<SendOutlined />} onClick={handleAsk} size="large" loading={loading}>
            发送
          </Button>
        </div>
      </Card>
    </div>
  );
};

export default QA;
