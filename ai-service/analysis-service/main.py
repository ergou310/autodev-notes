"""
AutoDev Notes - 学情分析服务
功能：课堂质量分析、学生知识点掌握分析、班级数据统计
"""
import os
import json
import math
from typing import Optional, List
from datetime import datetime

from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel

app = FastAPI(
    title="AutoDev Notes - 学情分析服务",
    description="课堂质量分析、知识点掌握分析、班级数据统计",
    version="1.0.0"
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# ── 内存存储 ──
grades_store = []  # [{student_id, course_id, scores: [{topic, score}]}]


# ── 数据模型 ──

class ClassroomAnalyzeRequest(BaseModel):
    text: str             # 课堂转写文本
    course_name: Optional[str] = None

class KnowledgeAnalysisRequest(BaseModel):
    student_id: str
    course_id: str
    answers: List[dict]   # [{topic, question, answer, score}]

class ClassReportRequest(BaseModel):
    course_id: str
    student_ids: Optional[List[str]] = None


# ── 工具函数 ──

def call_llm(prompt: str, system_prompt: str = "") -> str:
    import httpx
    api_key = os.getenv("OPENAI_API_KEY") or os.getenv("DEEPSEEK_API_KEY", "")
    base_url = os.getenv("OPENAI_BASE_URL") or os.getenv("DEEPSEEK_BASE_URL", "https://api.deepseek.com/v1")
    model = os.getenv("LLM_MODEL", "deepseek-chat")

    if not api_key:
        return _mock_llm(prompt)

    messages = []
    if system_prompt:
        messages.append({"role": "system", "content": system_prompt})
    messages.append({"role": "user", "content": prompt})

    try:
        with httpx.Client(timeout=60) as client:
            resp = client.post(
                f"{base_url}/chat/completions",
                headers={"Authorization": f"Bearer {api_key}"},
                json={"model": model, "messages": messages, "temperature": 0.3}
            )
            resp.raise_for_status()
            return resp.json()["choices"][0]["message"]["content"]
    except Exception:
        return _mock_llm(prompt)


def _mock_llm(prompt: str) -> str:
    if "知识点" in prompt:
        return json.dumps({
            "知识点A": {"掌握程度": "优秀", "正确率": 0.92},
            "知识点B": {"掌握程度": "良好", "正确率": 0.78},
            "知识点C": {"掌握程度": "需加强", "正确率": 0.55}
        }, ensure_ascii=False)
    return "分析完成"


def extract_keywords_simple(text: str, top_n: int = 10) -> List[dict]:
    """简易关键词提取（基于词频）"""
    import re
    # 去除标点和短词
    words = re.findall(r'[\u4e00-\u9fff]{2,}|[a-zA-Z]{3,}', text)
    freq = {}
    for w in words:
        freq[w] = freq.get(w, 0) + 1
    sorted_words = sorted(freq.items(), key=lambda x: x[1], reverse=True)
    return [{"word": w, "count": c} for w, c in sorted_words[:top_n]]


# ── 接口 ──

@app.get("/")
async def root():
    return {"message": "AutoDev Notes Analysis Service", "version": "1.0.0"}

@app.get("/health")
async def health():
    return {"status": "healthy"}


# ── 课堂质量分析 ──

@app.post("/analyze")
async def analyze_classroom(req: ClassroomAnalyzeRequest):
    """
    课堂质量分析
    输入：课堂转写文本
    输出：关键词提取、知识点识别、课堂摘要、质量评分
    """
    # 1. 关键词提取
    keywords = extract_keywords_simple(req.text)

    # 2. LLM 深度分析
    system_prompt = """你是课堂质量分析助手。请分析以下课堂转写内容，返回JSON格式：
{
    "summary": "课堂摘要（50字内）",
    "key_points": ["知识点1", "知识点2", ...],
    "teaching_quality": {
        "score": 85,
        "logic_score": 90,
        "interaction_score": 75,
        "coverage_score": 85,
        "comments": "评价说明"
    },
    "suggestions": ["建议1", "建议2"]
}"""

    result = call_llm(f"课程：{req.course_name or '未指定'}\n\n课堂内容：\n{req.text[:2000]}", system_prompt)

    try:
        if "{" in result:
            start = result.index("{")
            end = result.rindex("}") + 1
            analysis = json.loads(result[start:end])
        else:
            analysis = json.loads(result)
    except (json.JSONDecodeError, ValueError):
        analysis = {
            "summary": req.text[:100] + "...",
            "key_points": [kw["word"] for kw in keywords[:5]],
            "teaching_quality": {
                "score": 80, "logic_score": 82, "interaction_score": 75,
                "coverage_score": 80, "comments": "课堂内容覆盖较好"
            },
            "suggestions": ["建议增加互动环节", "可以补充更多案例"]
        }

    # 合并关键词
    analysis["keywords"] = keywords
    analysis["text_length"] = len(req.text)
    analysis["estimated_duration_min"] = max(1, len(req.text) // 300)

    return analysis


# ── 学生知识点掌握分析 ──

@app.post("/knowledge")
async def analyze_knowledge(req: KnowledgeAnalysisRequest):
    """
    学生知识点掌握情况分析
    输入：学生答题记录
    输出：各知识点掌握程度 + 可视化数据
    """
    # 按知识点分组统计
    topic_scores = {}
    for answer in req.answers:
        topic = answer.get("topic", "未分类")
        score = answer.get("score", 0)
        if topic not in topic_scores:
            topic_scores[topic] = {"total": 0, "count": 0, "scores": []}
        topic_scores[topic]["total"] += score
        topic_scores[topic]["count"] += 1
        topic_scores[topic]["scores"].append(score)

    # 计算掌握程度
    knowledge_map = {}
    for topic, data in topic_scores.items():
        avg = data["total"] / data["count"] if data["count"] > 0 else 0
        if avg >= 85:
            level = "优秀"
        elif avg >= 70:
            level = "良好"
        elif avg >= 60:
            level = "及格"
        else:
            level = "需加强"
        knowledge_map[topic] = {
            "average_score": round(avg, 1),
            "question_count": data["count"],
            "level": level,
            "scores": data["scores"]
        }

    # 存储
    grades_store.append({
        "student_id": req.student_id,
        "course_id": req.course_id,
        "knowledge_map": knowledge_map,
        "recorded_at": datetime.now().isoformat()
    })

    # 生成雷达图数据
    radar_data = {
        "labels": list(knowledge_map.keys()),
        "values": [v["average_score"] for v in knowledge_map.values()]
    }

    # 生成建议
    weak_topics = [t for t, v in knowledge_map.items() if v["average_score"] < 70]
    suggestions = []
    if weak_topics:
        suggestions.append(f"以下知识点需要重点复习：{', '.join(weak_topics)}")
    else:
        suggestions.append("整体掌握情况良好，建议保持学习节奏")

    return {
        "student_id": req.student_id,
        "knowledge_map": knowledge_map,
        "radar_chart": radar_data,
        "overall_average": round(
            sum(v["average_score"] for v in knowledge_map.values()) / len(knowledge_map), 1
        ) if knowledge_map else 0,
        "suggestions": suggestions
    }


# ── 班级数据统计 ──

@app.post("/report")
async def class_report(req: ClassReportRequest):
    """
    班级学情报告
    输出：成绩分布、平均分、及格率、知识点薄弱项
    """
    # 筛选该课程的成绩记录
    course_grades = [g for g in grades_store if g["course_id"] == req.course_id]
    if req.student_ids:
        course_grades = [g for g in course_grades if g["student_id"] in req.student_ids]

    if not course_grades:
        # 无真实数据时返回模拟报告
        return _mock_class_report(req.course_id)

    # 汇总统计
    all_scores = []
    topic_total = {}
    for g in course_grades:
        for topic, info in g.get("knowledge_map", {}).items():
            all_scores.append(info["average_score"])
            if topic not in topic_total:
                topic_total[topic] = {"sum": 0, "count": 0}
            topic_total[topic]["sum"] += info["average_score"]
            topic_total[topic]["count"] += 1

    # 成绩分布
    distribution = {"90-100": 0, "80-89": 0, "70-79": 0, "60-69": 0, "<60": 0}
    for s in all_scores:
        if s >= 90: distribution["90-100"] += 1
        elif s >= 80: distribution["80-89"] += 1
        elif s >= 70: distribution["70-79"] += 1
        elif s >= 60: distribution["60-69"] += 1
        else: distribution["<60"] += 1

    # 各知识点平均
    topic_averages = {
        t: round(d["sum"] / d["count"], 1)
        for t, d in topic_total.items()
    }

    weak_topics = [t for t, avg in topic_averages.items() if avg < 70]

    return {
        "course_id": req.course_id,
        "student_count": len(course_grades),
        "overall_average": round(sum(all_scores) / len(all_scores), 1) if all_scores else 0,
        "pass_rate": round(len([s for s in all_scores if s >= 60]) / len(all_scores) * 100, 1) if all_scores else 0,
        "distribution": distribution,
        "topic_averages": topic_averages,
        "weak_topics": weak_topics,
        "suggestions": [
            f"建议对「{t}」进行专项复习" for t in weak_topics
        ] if weak_topics else ["班级整体表现良好"]
    }


def _mock_class_report(course_id: str) -> dict:
    return {
        "course_id": course_id,
        "student_count": 45,
        "overall_average": 76.5,
        "pass_rate": 88.9,
        "distribution": {"90-100": 8, "80-89": 15, "70-79": 12, "60-69": 7, "<60": 3},
        "topic_averages": {"知识点A": 82.3, "知识点B": 75.6, "知识点C": 68.2, "知识点D": 79.1},
        "weak_topics": ["知识点C"],
        "suggestions": ["建议对「知识点C」进行专项复习"],
        "note": "（模拟数据，需录入真实成绩后生成实际报告）"
    }
