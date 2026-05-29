"""
Tests for AutoDev Notes - AI Note Generator Service
Run: pytest test_main.py -v
"""
import json
import os
import pytest
from unittest.mock import patch

from httpx import AsyncClient, ASGITransport
from main import app, text_to_mindmap_json, _mock_llm_response


@pytest.fixture
def anyio_backend():
    return "asyncio"


@pytest.fixture
async def client():
    transport = ASGITransport(app=app)
    async with AsyncClient(transport=transport, base_url="http://test") as ac:
        yield ac


# ── GET / ──

@pytest.mark.anyio
async def test_root(client):
    resp = await client.get("/")
    assert resp.status_code == 200
    data = resp.json()
    assert data["version"] == "1.0.0"
    assert "AI" in data["message"] or "Note" in data["message"]


# ── GET /health ──

@pytest.mark.anyio
async def test_health(client):
    resp = await client.get("/health")
    assert resp.status_code == 200
    assert resp.json()["status"] == "healthy"


# ── POST /transcribe ──

@pytest.mark.anyio
async def test_transcribe_mock(client):
    """Without whisper installed, endpoint returns mock data."""
    resp = await client.post(
        "/transcribe",
        files={"file": ("test.wav", b"fake-audio", "audio/wav")},
    )
    assert resp.status_code == 200
    data = resp.json()
    assert "text" in data
    assert data["language"] == "zh"
    assert "segments" in data


# ── POST /generate ──

@pytest.mark.anyio
async def test_generate_note_basic(client):
    resp = await client.post(
        "/generate",
        json={"text": "今天我们学习了Python编程基础"},
    )
    assert resp.status_code == 200
    data = resp.json()
    assert data["title"] == "课堂笔记"
    assert len(data["content"]) > 0
    assert data["source"] == "AI_GENERATED"
    assert data["word_count"] > 0


@pytest.mark.anyio
async def test_generate_note_with_options(client):
    resp = await client.post(
        "/generate",
        json={
            "text": "机器学习基础概念",
            "title": "ML课程笔记",
            "course_name": "机器学习",
            "language": "zh",
        },
    )
    assert resp.status_code == 200
    data = resp.json()
    assert data["title"] == "ML课程笔记"


@pytest.mark.anyio
async def test_generate_note_mock_content(client):
    """Mock LLM returns structured markdown."""
    resp = await client.post("/generate", json={"text": "任意内容"})
    content = resp.json()["content"]
    assert "# 课堂笔记" in content or "##" in content


# ── POST /mindmap ──

@pytest.mark.anyio
async def test_mindmap_basic(client):
    resp = await client.post(
        "/mindmap",
        json={"content": "学习了机器学习的基本概念"},
    )
    assert resp.status_code == 200
    data = resp.json()
    assert "mindmap" in data
    assert "title" in data["mindmap"]
    assert "children" in data["mindmap"]


@pytest.mark.anyio
async def test_mindmap_with_title(client):
    resp = await client.post(
        "/mindmap",
        json={"content": "神经网络", "title": "AI思维导图"},
    )
    assert resp.status_code == 200
    assert resp.json()["title"] == "AI思维导图"


@pytest.mark.anyio
async def test_mindmap_structure(client):
    """Mindmap result always has title and children fields."""
    resp = await client.post("/mindmap", json={"content": "思维导图测试"})
    mm = resp.json()["mindmap"]
    assert "title" in mm
    assert "children" in mm
    assert isinstance(mm["children"], list)


# ── POST /quiz ──

@pytest.mark.anyio
async def test_quiz_basic(client):
    resp = await client.post(
        "/quiz",
        json={"content": "机器学习包括监督学习和无监督学习"},
    )
    assert resp.status_code == 200
    data = resp.json()
    assert "questions" in data
    assert "total" in data
    assert data["difficulty"] == "medium"
    assert data["total"] > 0


@pytest.mark.anyio
async def test_quiz_custom_params(client):
    resp = await client.post(
        "/quiz",
        json={
            "content": "深度学习基础",
            "num_questions": 3,
            "difficulty": "hard",
        },
    )
    assert resp.status_code == 200
    data = resp.json()
    assert data["difficulty"] == "hard"
    # Mock LLM returns fixed 5 questions; num_questions is only used as LLM hint
    assert len(data["questions"]) > 0


@pytest.mark.anyio
async def test_quiz_question_fields(client):
    """Each question should have the required fields."""
    resp = await client.post("/quiz", json={"content": "内容"})
    for q in resp.json()["questions"]:
        assert "question" in q
        assert "answer" in q
        assert "type" in q
        assert "difficulty" in q


# ── Mock LLM helper ──

def test_mock_llm_response_mindmap():
    resp = _mock_llm_response("请生成思维导图")
    data = json.loads(resp)
    assert "children" in data
    assert "title" in data


def test_mock_llm_response_quiz():
    resp = _mock_llm_response("请生成复习题")
    data = json.loads(resp)
    assert isinstance(data, list)
    assert len(data) > 0
    assert "question" in data[0]


def test_mock_llm_response_default():
    resp = _mock_llm_response("随便什么内容")
    assert "# 课堂笔记" in resp


# ── text_to_mindmap_json ──

def test_text_to_mindmap_json_basic():
    md = "# My Note\n## Section 1\n- Item A\n- Item B\n## Section 2\n"
    result = text_to_mindmap_json(md)
    assert result["title"] == "My Note"
    assert len(result["children"]) == 2
    assert result["children"][0]["title"] == "Section 1"
    assert len(result["children"][0]["children"]) == 2


def test_text_to_mindmap_json_with_subsections():
    md = "# Root\n## A\n### Sub\n- bullet\n"
    result = text_to_mindmap_json(md)
    section_a = result["children"][0]
    assert section_a["title"] == "A"
    assert section_a["children"][0]["title"] == "Sub"
    assert section_a["children"][0]["children"][0]["title"] == "bullet"


def test_text_to_mindmap_json_empty():
    result = text_to_mindmap_json("")
    assert result["title"] == "笔记"
    assert result["children"] == []
