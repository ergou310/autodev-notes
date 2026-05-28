import React, { useState } from "react";
import { Card, Input, Button, Row, Col, Statistic, Tag, Spin, message, Divider } from "antd";
import { BarChartOutlined, BulbOutlined } from "@ant-design/icons";
import { aiApi } from "../api/ai";

const { TextArea } = Input;

const Analysis: React.FC = () => {
  const [text, setText] = useState("");
  const [courseName, setCourseName] = useState("");
  const [result, setResult] = useState<any>(null);
  const [loading, setLoading] = useState(false);

  const handleAnalyze = async () => {
    if (!text.trim()) { message.warning("请输入课堂内容"); return; }
    setLoading(true);
    try {
      const res: any = await aiApi.analyzeClassroom({ text, courseName });
      setResult(res.data || res);
    } catch { message.error("分析失败"); }
    finally { setLoading(false); }
  };

  return (
    <div>
      <Card title="课堂质量分析" style={{ marginBottom: 16 }}>
        <Input placeholder="课程名称（选填）" value={courseName} onChange={(e) => setCourseName(e.target.value)} style={{ marginBottom: 8, width: 300 }} />
        <TextArea value={text} onChange={(e) => setText(e.target.value)} placeholder="粘贴课堂转写文本..." rows={6} />
        <Button type="primary" icon={<BarChartOutlined />} onClick={handleAnalyze} style={{ marginTop: 8 }} loading={loading}>
          开始分析
        </Button>
      </Card>

      <Spin spinning={loading}>
        {result && (
          <>
            {/* 评分卡片 */}
            <Row gutter={16} style={{ marginBottom: 16 }}>
              <Col span={6}>
                <Card><Statistic title="综合评分" value={result.teaching_quality?.score || 0} suffix="/ 100" valueStyle={{ color: "#1890ff" }} /></Card>
              </Col>
              <Col span={6}>
                <Card><Statistic title="逻辑性" value={result.teaching_quality?.logic_score || 0} suffix="/ 100" /></Card>
              </Col>
              <Col span={6}>
                <Card><Statistic title="互动性" value={result.teaching_quality?.interaction_score || 0} suffix="/ 100" /></Card>
              </Col>
              <Col span={6}>
                <Card><Statistic title="覆盖率" value={result.teaching_quality?.coverage_score || 0} suffix="/ 100" /></Card>
              </Col>
            </Row>

            {/* 课堂摘要 */}
            <Card title="课堂摘要" style={{ marginBottom: 16 }}>
              <p>{result.summary}</p>
              <Divider />
              <p><strong>预估时长：</strong>{result.estimated_duration_min} 分钟</p>
              <p><strong>文本长度：</strong>{result.text_length} 字</p>
            </Card>

            {/* 关键词 */}
            <Card title="关键词提取" style={{ marginBottom: 16 }}>
              {(result.keywords || []).map((kw: any, i: number) => (
                <Tag key={i} color="blue" style={{ marginBottom: 4 }}>
                  {kw.word} ({kw.count})
                </Tag>
              ))}
            </Card>

            {/* 知识点 */}
            <Card title="识别知识点" style={{ marginBottom: 16 }}>
              {(result.key_points || []).map((kp: string, i: number) => (
                <Tag key={i} color="green" style={{ marginBottom: 4 }}>{kp}</Tag>
              ))}
            </Card>

            {/* 改进建议 */}
            <Card title="改进建议">
              <ul>
                {(result.suggestions || []).map((s: string, i: number) => (
                  <li key={i}><BulbOutlined /> {s}</li>
                ))}
              </ul>
              {result.teaching_quality?.comments && (
                <p style={{ marginTop: 8, color: "#666" }}>评语：{result.teaching_quality.comments}</p>
              )}
            </Card>
          </>
        )}
      </Spin>
    </div>
  );
};

export default Analysis;
