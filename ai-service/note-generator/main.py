"""
AutoDev Notes - AI 笔记生成服务
功能：语音转文字、笔记生成、思维导图生成、复习题生成
"""
import os
import json
import uuid
from datetime import datetime
from typing import Optional

from fastapi import FastAPI, UploadFile, File, Form, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel

app = FastAPI(
    title="AutoDev Notes - AI 笔记生成服务",
    description="语音转文字、笔记生成、思维导图、复习题生成",
    version="1.0.0"
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# ── 存储（开发环境用内存，生产环境换数据库） ──
task_store = {}


# ── 数据模型 ──

class TranscribeRequest(BaseModel):
    file_path: str  # 文件路径或URL

class NoteGenerateRequest(BaseModel):
    text: str  # 原始文本（语音转写结果或手动输入）
    title: Optional[str] = None
    course_name: Optional[str] = None
    language: str = "zh"

class MindMapRequest(BaseModel):
    content: str  # 笔记内容
    title: Optional[str] = None

class QuizRequest(BaseModel):
    content: str  # 笔记内容
    num_questions: int = 5
    difficulty: str = "medium"  # easy / medium / hard

class TaskResponse(BaseModel):
    task_id: str
    status: str
    result: Optional[dict] = None
    error: Optional[str] = None


# ── 工具函数 ──

def call_llm(prompt: str, system_prompt: str = "") -> str:
    """
    调用大模型API（兼容 OpenAI 格式）
    优先使用环境变量配置：
      - OPENAI_API_KEY / OPENAI_BASE_URL
      - 或 DEEPSEEK_API_KEY / DEEPSEEK_BASE_URL
    """
    import httpx

    # 选择 provider
    api_key = os.getenv("OPENAI_API_KEY") or os.getenv("DEEPSEEK_API_KEY", "")
    base_url = os.getenv("OPENAI_BASE_URL") or os.getenv("DEEPSEEK_BASE_URL", "https://api.deepseek.com/v1")
    model = os.getenv("LLM_MODEL", "deepseek-chat")

    if not api_key:
        # 无API Key时返回模拟数据
        return _mock_llm_response(prompt)

    messages = []
    if system_prompt:
        messages.append({"role": "system", "content": system_prompt})
    messages.append({"role": "user", "content": prompt})

    try:
        with httpx.Client(timeout=60) as client:
            resp = client.post(
                f"{base_url}/chat/completions",
                headers={"Authorization": f"Bearer {api_key}"},
                json={"model": model, "messages": messages, "temperature": 0.7}
            )
            resp.raise_for_status()
            return resp.json()["choices"][0]["message"]["content"]
    except Exception as e:
        return _mock_llm_response(prompt)


def _mock_llm_response(prompt: str) -> str:
    """无API Key时返回模拟数据，方便开发调试"""
    if "思维导图" in prompt or "mindmap" in prompt.lower():
        return json.dumps({
            "title": "课程笔记",
            "children": [
                {"title": "核心概念", "children": [
                    {"title": "概念A", "children": []},
                    {"title": "概念B", "children": []}
                ]},
                {"title": "重点内容", "children": [
                    {"title": "要点1", "children": []},
                    {"title": "要点2", "children": []}
                ]},
                {"title": "总结", "children": []}
            ]
        }, ensure_ascii=False)
    elif "复习题" in prompt or "quiz" in prompt.lower():
        return json.dumps([
            {"question": "请简述本节课的核心概念", "answer": "本节课的核心概念包括...", "type": "short_answer", "difficulty": "medium"},
            {"question": "概念A和概念B的区别是什么？", "answer": "概念A侧重于..., 而概念B侧重于...", "type": "short_answer", "difficulty": "medium"},
            {"question": "请举例说明要点1的实际应用", "answer": "在实际场景中...", "type": "short_answer", "difficulty": "hard"},
            {"question": "以下哪个描述是正确的？", "answer": "选项B", "type": "choice", "difficulty": "easy"},
            {"question": "请总结本节课的主要内容", "answer": "本节课主要讲了...", "type": "short_answer", "difficulty": "easy"}
        ], ensure_ascii=False)
    else:
        return """# 课堂笔记

## 一、核心概念
- 概念A：详细说明
- 概念B：详细说明

## 二、重点内容
1. 要点1：展开描述
2. 要点2：展开描述

## 三、案例分析
- 案例说明与应用场景

## 四、总结
- 本节课主要学习了以上内容，重点掌握核心概念和实际应用。
"""


def text_to_mindmap_json(markdown_text: str) -> dict:
    """将 Markdown 文本转换为思维导图 JSON 格式（Mind Elixir 兼容）"""
    lines = markdown_text.strip().split("\n")
    root = {"title": "笔记", "children": []}
    current_section = None
    current_sub = None

    for line in lines:
        line = line.strip()
        if not line:
            continue
        if line.startswith("# ") and not line.startswith("## "):
            root["title"] = line[2:].strip()
        elif line.startswith("## "):
            current_section = {"title": line[3:].strip(), "children": []}
            root["children"].append(current_section)
            current_sub = None
        elif line.startswith("### "):
            if current_section:
                current_sub = {"title": line[4:].strip(), "children": []}
                current_section["children"].append(current_sub)
        elif line.startswith("- ") or line.startswith("* "):
            item = {"title": line[2:].strip(), "children": []}
            if current_sub:
                current_sub["children"].append(item)
            elif current_section:
                current_section["children"].append(item)

    return root


# ── 接口 ──

@app.get("/")
async def root():
    return {"message": "AutoDev Notes AI Note Generator", "version": "1.0.0"}

@app.get("/health")
async def health():
    return {"status": "healthy"}


@app.post("/transcribe")
async def transcribe_audio(file: UploadFile = File(...)):
    """
    语音转文字（Whisper）
    接收音频文件，返回转写文本
    """
    try:
        import whisper

        # 保存临时文件
        temp_path = f"/tmp/{uuid.uuid4()}_{file.filename}"
        content = await file.read()
        with open(temp_path, "wb") as f:
            f.write(content)

        # Whisper 转写
        model = whisper.load_model("base")
        result = model.transcribe(temp_path, language="zh")
        text = result["text"]

        # 清理临时文件
        os.remove(temp_path)

        return {
            "text": text,
            "language": result.get("language", "zh"),
            "segments": result.get("segments", [])
        }
    except ImportError:
        # Whisper 未安装时返回模拟数据
        return {
            "text": "（Whisper未安装，返回模拟数据）今天这节课我们学习了机器学习的基本概念，包括监督学习、无监督学习和强化学习。监督学习是通过标注数据训练模型，无监督学习是发现数据中的隐藏模式，强化学习是通过与环境交互来学习最优策略。",
            "language": "zh",
            "segments": []
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"语音转文字失败: {str(e)}")


@app.post("/generate")
async def generate_note(req: NoteGenerateRequest):
    """
    生成结构化笔记
    输入：转写文本 / 手动输入文本
    输出：Markdown 格式笔记
    """
    system_prompt = """你是一个专业的课堂笔记整理助手。请将以下课堂内容整理成结构化的Markdown笔记。
要求：
1. 使用清晰的标题层级（# ## ###）
2. 提取核心概念和关键要点
3. 保留重要的例子和案例
4. 在最后添加总结部分
5. 笔记要简洁精炼，突出重点"""

    user_prompt = f"""请将以下课堂内容整理成结构化笔记：

课程名称：{req.course_name or '未指定'}
原始内容：
{req.text}

请生成Markdown格式的笔记："""

    note_content = call_llm(user_prompt, system_prompt)

    return {
        "title": req.title or "课堂笔记",
        "content": note_content,
        "source": "AI_GENERATED",
        "word_count": len(note_content)
    }


@app.post("/mindmap")
async def generate_mindmap(req: MindMapRequest):
    """
    生成思维导图
    输入：笔记内容
    输出：思维导图 JSON（Mind Elixir 格式）
    """
    # 先用 LLM 提取结构化大纲
    system_prompt = "你是思维导图生成助手。请将以下内容提取为层级大纲，用Markdown格式输出。"
    user_prompt = f"""请将以下内容转换为思维导图大纲：

{req.content}

要求：用Markdown的标题层级表示，## 为一级节点，### 为二级节点，- 为三级节点。"""

    outline = call_llm(user_prompt, system_prompt)

    # 转换为思维导图 JSON
    mindmap_data = text_to_mindmap_json(outline)

    return {
        "title": req.title or mindmap_data.get("title", "思维导图"),
        "mindmap": mindmap_data,
        "outline": outline
    }


@app.post("/quiz")
async def generate_quiz(req: QuizRequest):
    """
    生成复习题
    输入：笔记内容
    输出：题目+答案列表
    """
    system_prompt = """你是复习题生成助手。根据给定内容生成复习题。
要求：
1. 题型包括：简答题(short_answer)、选择题(choice)、判断题(true_false)
2. 每道题包含：question（题目）、answer（答案）、type（题型）、difficulty（难度）
3. 返回JSON数组格式"""

    user_prompt = f"""根据以下内容生成 {req.num_questions} 道复习题，难度：{req.difficulty}

内容：
{req.content}

请直接返回JSON数组："""

    quiz_text = call_llm(user_prompt, system_prompt)

    # 解析为JSON
    try:
        # 尝试提取JSON部分
        if "[" in quiz_text:
            start = quiz_text.index("[")
            end = quiz_text.rindex("]") + 1
            questions = json.loads(quiz_text[start:end])
        else:
            questions = json.loads(quiz_text)
    except (json.JSONDecodeError, ValueError):
        # 解析失败时返回结构化数据
        questions = [
            {"question": q.strip(), "answer": "参考答案", "type": "short_answer", "difficulty": req.difficulty}
            for q in quiz_text.split("\n") if q.strip() and len(q.strip()) > 5
        ][:req.num_questions]

    return {
        "questions": questions,
        "total": len(questions),
        "difficulty": req.difficulty
    }
