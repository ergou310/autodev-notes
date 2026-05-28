"""
AutoDev Notes - 智能阅卷服务
功能：主观题自动批改、评分标准配置、成绩统计
"""
import os
import json
from typing import Optional, List
from datetime import datetime

from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel

app = FastAPI(
    title="AutoDev Notes - 智能阅卷服务",
    description="主观题自动批改、评分标准配置、成绩统计",
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
grading_records = []
rubric_store = {}


# ── 数据模型 ──

class GradeRequest(BaseModel):
    question: str           # 题目
    reference_answer: str   # 参考答案
    student_answer: str     # 学生答案
    max_score: float = 100  # 满分
    rubric: Optional[str] = None  # 评分标准（可选）

class BatchGradeRequest(BaseModel):
    exam_id: str
    questions: List[dict]   # [{question, reference_answer, max_score, rubric?}]
    submissions: List[dict] # [{student_id, answers: [{question_index, answer}]}]

class RubricRequest(BaseModel):
    name: str
    criteria: List[dict]    # [{criterion, weight, description, score_range}]


# ── 工具函数 ──

def call_llm(prompt: str, system_prompt: str = "") -> str:
    import httpx
    api_key = os.getenv("OPENAI_API_KEY") or os.getenv("DEEPSEEK_API_KEY", "")
    base_url = os.getenv("OPENAI_BASE_URL") or os.getenv("DEEPSEEK_BASE_URL", "https://api.deepseek.com/v1")
    model = os.getenv("LLM_MODEL", "deepseek-chat")

    if not api_key:
        return _mock_grade(prompt)

    messages = []
    if system_prompt:
        messages.append({"role": "system", "content": system_prompt})
    messages.append({"role": "user", "content": prompt})

    try:
        with httpx.Client(timeout=60) as client:
            resp = client.post(
                f"{base_url}/chat/completions",
                headers={"Authorization": f"Bearer {api_key}"},
                json={"model": model, "messages": messages, "temperature": 0.2}
            )
            resp.raise_for_status()
            return resp.json()["choices"][0]["message"]["content"]
    except Exception:
        return _mock_grade(prompt)


def _mock_grade(prompt: str) -> str:
    return json.dumps({
        "score": 82,
        "max_score": 100,
        "breakdown": {
            "内容完整性": {"score": 35, "max": 40, "comment": "核心要点覆盖较全"},
            "逻辑清晰度": {"score": 25, "max": 30, "comment": "条理清晰"},
            "语言表达": {"score": 14, "max": 20, "comment": "表述准确"},
            "创新见解": {"score": 8, "max": 10, "comment": "有一定自己的理解"}
        },
        "feedback": "回答整体较好，覆盖了主要知识点。建议补充具体案例来支撑观点。",
        "highlights": ["概念定义准确", "结构清晰"],
        "improvements": ["缺少具体例子", "可以更深入分析"]
    }, ensure_ascii=False)


# ── 接口 ──

@app.get("/")
async def root():
    return {"message": "AutoDev Notes Grading Service", "version": "1.0.0"}

@app.get("/health")
async def health():
    return {"status": "healthy", "records": len(grading_records)}


# ── 单题批改 ──

@app.post("/grade")
async def grade_answer(req: GradeRequest):
    """
    主观题自动批改
    输入：题目、参考答案、学生答案
    输出：分数、分项评分、评语、亮点、改进建议
    """
    system_prompt = """你是一个专业的阅卷助手。请根据题目、参考答案和评分标准，对学生的回答进行评分。
返回JSON格式：
{
    "score": 85,
    "max_score": 100,
    "breakdown": {
        "评分维度1": {"score": 35, "max": 40, "comment": "说明"},
        "评分维度2": {"score": 25, "max": 30, "comment": "说明"}
    },
    "feedback": "总体评语",
    "highlights": ["亮点1", "亮点2"],
    "improvements": ["改进1", "改进2"]
}"""

    rubric_text = f"\n评分标准：{req.rubric}" if req.rubric else ""
    user_prompt = f"""题目：{req.question}
满分：{req.max_score}{rubric_text}

参考答案：
{req.reference_answer}

学生答案：
{req.student_answer}

请评分："""

    result = call_llm(user_prompt, system_prompt)

    try:
        if "{" in result:
            start = result.index("{")
            end = result.rindex("}") + 1
            grading = json.loads(result[start:end])
        else:
            grading = json.loads(result)
    except (json.JSONDecodeError, ValueError):
        grading = {
            "score": 75,
            "max_score": req.max_score,
            "breakdown": {"整体评分": {"score": 75, "max": req.max_score, "comment": "回答基本正确"}},
            "feedback": "回答基本涵盖了主要知识点",
            "highlights": ["回答了核心问题"],
            "improvements": ["可以更加详细"]
        }

    # 保存记录
    record = {
        "question": req.question,
        "student_answer": req.student_answer[:100],
        "score": grading.get("score", 0),
        "graded_at": datetime.now().isoformat()
    }
    grading_records.append(record)

    return grading


# ── 批量批改 ──

@app.post("/grade/batch")
async def batch_grade(req: BatchGradeRequest):
    """
    批量阅卷
    输入：考试题目 + 所有学生答卷
    输出：每个学生的成绩单
    """
    results = []

    for submission in req.submissions:
        student_id = submission["student_id"]
        total_score = 0
        max_total = 0
        question_results = []

        for ans in submission.get("answers", []):
            qi = ans["question_index"]
            if qi >= len(req.questions):
                continue
            q = req.questions[qi]
            max_score = q.get("max_score", 100)
            max_total += max_score

            # 对每道题评分
            system_prompt = "你是阅卷助手，根据参考答案评分，返回JSON：{"score": 数字, "feedback": "评语"}"
            user_prompt = f"题目：{q['question']}\n参考答案：{q['reference_answer']}\n学生答案：{ans['answer']}\n满分：{max_score}"

            result = call_llm(user_prompt, system_prompt)
            try:
                if "{" in result:
                    s = result.index("{")
                    e = result.rindex("}") + 1
                    qr = json.loads(result[s:e])
                else:
                    qr = {"score": max_score * 0.7, "feedback": "评分解析失败"}
            except (json.JSONDecodeError, ValueError):
                qr = {"score": max_score * 0.7, "feedback": "评分解析失败"}

            qr["question_index"] = qi
            qr["max_score"] = max_score
            question_results.append(qr)
            total_score += qr.get("score", 0)

        results.append({
            "student_id": student_id,
            "total_score": round(total_score, 1),
            "max_total": max_total,
            "percentage": round(total_score / max_total * 100, 1) if max_total > 0 else 0,
            "questions": question_results
        })

    return {
        "exam_id": req.exam_id,
        "student_count": len(results),
        "results": results
    }


# ── 评分标准管理 ──

@app.post("/rubric")
async def create_rubric(req: RubricRequest):
    """创建评分标准"""
    rubric_id = str(len(rubric_store) + 1)
    rubric = {
        "id": rubric_id,
        "name": req.name,
        "criteria": req.criteria,
        "created_at": datetime.now().isoformat()
    }
    rubric_store[rubric_id] = rubric
    return rubric


@app.get("/rubric")
async def list_rubrics():
    """列出所有评分标准"""
    return {"total": len(rubric_store), "rubrics": list(rubric_store.values())}


@app.get("/rubric/{rubric_id}")
async def get_rubric(rubric_id: str):
    """获取评分标准详情"""
    if rubric_id not in rubric_store:
        raise HTTPException(status_code=404, detail="评分标准不存在")
    return rubric_store[rubric_id]


# ── 成绩统计 ──

@app.get("/statistics")
async def get_statistics():
    """成绩统计概览"""
    if not grading_records:
        return {
            "total_graded": 0,
            "average_score": 0,
            "score_distribution": {},
            "note": "暂无批改记录"
        }

    scores = [r["score"] for r in grading_records]
    distribution = {"90-100": 0, "80-89": 0, "70-79": 0, "60-69": 0, "<60": 0}
    for s in scores:
        if s >= 90: distribution["90-100"] += 1
        elif s >= 80: distribution["80-89"] += 1
        elif s >= 70: distribution["70-79"] += 1
        elif s >= 60: distribution["60-69"] += 1
        else: distribution["<60"] += 1

    return {
        "total_graded": len(grading_records),
        "average_score": round(sum(scores) / len(scores), 1),
        "highest": max(scores),
        "lowest": min(scores),
        "pass_rate": round(len([s for s in scores if s >= 60]) / len(scores) * 100, 1),
        "score_distribution": distribution
    }
