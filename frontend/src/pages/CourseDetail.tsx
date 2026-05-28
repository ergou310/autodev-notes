import React, { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import { Descriptions, Tag, Button, message, Spin, Divider, List } from "antd";
import { courseApi } from "../api/course";

const CourseDetail: React.FC = () => {
  const { id } = useParams();
  const [course, setCourse] = useState<any>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!id) return;
    courseApi.getById(Number(id))
      .then((res: any) => setCourse(res.data))
      .catch(() => message.error("加载失败"))
      .finally(() => setLoading(false));
  }, [id]);

  if (loading) return <Spin size="large" style={{ display: "block", margin: "100px auto" }} />;
  if (!course) return <div>课程不存在</div>;

  return (
    <div>
      <Descriptions title={course.name} bordered column={2}>
        <Descriptions.Item label="教师">{course.teacherName}</Descriptions.Item>
        <Descriptions.Item label="状态">
          <Tag color={course.status === "PUBLISHED" ? "green" : "default"}>
            {course.status === "PUBLISHED" ? "已发布" : course.status === "CLOSED" ? "已关闭" : "草稿"}
          </Tag>
        </Descriptions.Item>
        <Descriptions.Item label="分类">{course.category || "未分类"}</Descriptions.Item>
        <Descriptions.Item label="学分">{course.credit || "-"}</Descriptions.Item>
        <Descriptions.Item label="描述" span={2}>{course.description || "暂无描述"}</Descriptions.Item>
      </Descriptions>

      <Divider />

      <div style={{ display: "flex", gap: 16 }}>
        <Button type="primary">选课</Button>
        <Button>查看笔记</Button>
        <Button>查看班级</Button>
      </div>
    </div>
  );
};

export default CourseDetail;
