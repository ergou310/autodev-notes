import React, { useEffect, useState } from "react";
import { Card, Row, Col, Tag, Input, Button, Empty, Spin, message } from "antd";
import { PlusOutlined, SearchOutlined } from "@ant-design/icons";
import { useNavigate } from "react-router-dom";
import { courseApi } from "../api/course";

const { Meta } = Card;

interface Course {
  id: number;
  name: string;
  description: string;
  teacherName: string;
  category: string;
  credit: number;
  status: string;
}

const statusMap: Record<string, { color: string; text: string }> = {
  DRAFT: { color: "default", text: "草稿" },
  PUBLISHED: { color: "green", text: "已发布" },
  CLOSED: { color: "red", text: "已关闭" },
};

const CourseList: React.FC = () => {
  const [courses, setCourses] = useState<Course[]>([]);
  const [loading, setLoading] = useState(true);
  const [keyword, setKeyword] = useState("");
  const navigate = useNavigate();

  const loadCourses = async () => {
    setLoading(true);
    try {
      const res: any = await courseApi.getAll();
      setCourses(res.data || []);
    } catch {
      message.error("加载课程失败");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { loadCourses(); }, []);

  const handleSearch = async () => {
    if (!keyword.trim()) { loadCourses(); return; }
    setLoading(true);
    try {
      const res: any = await courseApi.search(keyword);
      setCourses(res.data || []);
    } catch {
      message.error("搜索失败");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div>
      <div style={{ display: "flex", justifyContent: "space-between", marginBottom: 16 }}>
        <Input.Search
          placeholder="搜索课程"
          value={keyword}
          onChange={(e) => setKeyword(e.target.value)}
          onSearch={handleSearch}
          style={{ width: 300 }}
        />
        <Button type="primary" icon={<PlusOutlined />} onClick={() => message.info("创建课程功能开发中")}>
          创建课程
        </Button>
      </div>

      <Spin spinning={loading}>
        {courses.length === 0 && !loading ? (
          <Empty description="暂无课程" />
        ) : (
          <Row gutter={[16, 16]}>
            {courses.map((course) => (
              <Col key={course.id} xs={24} sm={12} md={8} lg={6}>
                <Card
                  hoverable
                  onClick={() => navigate(`/courses/${course.id}`)}
                  cover={
                    <div style={{ height: 120, background: "linear-gradient(135deg, #667eea 0%, #764ba2 100%)", display: "flex", alignItems: "center", justifyContent: "center" }}>
                      <span style={{ color: "#fff", fontSize: 24, fontWeight: "bold" }}>{course.name[0]}</span>
                    </div>
                  }
                >
                  <Meta
                    title={course.name}
                    description={
                      <>
                        <div style={{ marginBottom: 8 }}>{course.description?.slice(0, 50)}{course.description?.length > 50 ? "..." : ""}</div>
                        <div>
                          <Tag>{course.teacherName || "未知教师"}</Tag>
                          <Tag color={statusMap[course.status]?.color}>{statusMap[course.status]?.text}</Tag>
                          {course.credit && <Tag color="blue">{course.credit}学分</Tag>}
                        </div>
                      </>
                    }
                  />
                </Card>
              </Col>
            ))}
          </Row>
        )}
      </Spin>
    </div>
  );
};

export default CourseList;
