"""
AutoDev Notes - RAG 检索服务
功能：文档向量化、智能问答、知识图谱、知识点推荐
"""
import os
import json
import uuid
from typing import Optional, List
from datetime import datetime

from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel

app = FastAPI(
    title="AutoDev Notes - RAG 检索服务",
    description="文档向量化、智能问答、知识图谱、知识点推荐",
    version="1.0.0"
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# ── 内存存储（开发环境，生产环境换 Chroma / Neo4j） ──
document_store = []      # [{id, title, content, chunks, metadata}]
knowledge_graph = {      # 简易知识图谱
    "nodes": [],         # [{id, label, type, properties}]
    "edges": []          # [{source, target, relation}]
}


# ── 数据模型 ──

class IndexRequest(BaseModel):
    title: str
    content: str
    doc_type: str = "textbook"  # textbook / slides / notes
    course_id: Optional[str] = None
    metadata: Optional[dict] = None

class AskRequest(BaseModel):
    question: str
    course_id: Optional[str] = None
    top_k: int = 3

class KgNodeRequest(BaseModel):
    label: str
    node_type: str = "concept"  # concept / topic / chapter
    properties: Optional[dict] = None

class KgEdgeRequest(BaseModel):
    source_id: str
    target_id: str
    relation: str  # is_part_of / depends_on / related_to / example_of

class RecommendRequest(BaseModel):
    topic: str
    course_id: Optional[str] = None
    num_recommendations: int = 5


# ── 工具函数 ──

def call_llm(prompt: str, system_prompt: str = "") -> str:
    """调用大模型（复用 note-generator 的逻辑）"""
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
    if "推荐" in prompt or "recommend" in prompt.lower():
        return json.dumps([
            {"title": "相关知识点A", "reason": "与当前主题密切相关", "relevance": 0.95},
            {"title": "相关知识点B", "reason": "是理解当前主题的基础", "relevance": 0.88},
            {"title": "相关知识点C", "reason": "当前主题的延伸应用", "relevance": 0.82},
            {"title": "相关知识点D", "reason": "常见的对比学习内容", "relevance": 0.75},
            {"title": "相关知识点E", "reason": "历史背景知识", "relevance": 0.68}
        ], ensure_ascii=False)
    return "根据检索到的相关文档内容，以下是回答：\n\n" + prompt[:200] + "...\n\n（注：这是模拟回答，配置API Key后将返回真实LLM回答）"


def chunk_text(text: str, chunk_size: int = 500, overlap: int = 50) -> List[str]:
    """文本分块"""
    chunks = []
    start = 0
    while start < len(text):
        end = start + chunk_size
        chunk = text[start:end]
        if chunk.strip():
            chunks.append(chunk.strip())
        start = end - overlap
    return chunks


def simple_search(query: str, top_k: int = 3) -> List[dict]:
    """简易关键词检索（开发环境，生产环境换向量检索）"""
    results = []
    query_lower = query.lower()
    for doc in document_store:
        for i, chunk in enumerate(doc["chunks"]):
            # 简单关键词匹配打分
            score = sum(1 for word in query_lower.split() if word in chunk.lower())
            if score > 0:
                results.append({
                    "doc_id": doc["id"],
                    "doc_title": doc["title"],
                    "chunk_index": i,
                    "content": chunk,
                    "score": score / len(query_lower.split())
                })
    results.sort(key=lambda x: x["score"], reverse=True)
    return results[:top_k]


# ── 接口 ──

@app.get("/")
async def root():
    return {"message": "AutoDev Notes RAG Service", "version": "1.0.0"}

@app.get("/health")
async def health():
    return {"status": "healthy", "documents": len(document_store), "graph_nodes": len(knowledge_graph["nodes"])}


# ── 文档索引 ──

@app.post("/index")
async def index_document(req: IndexRequest):
    """
    文档向量化索引
    将文档分块存储，供后续检索使用
    """
    doc_id = str(uuid.uuid4())[:8]
    chunks = chunk_text(req.content)

    doc = {
        "id": doc_id,
        "title": req.title,
        "content": req.content,
        "chunks": chunks,
        "doc_type": req.doc_type,
        "course_id": req.course_id,
        "metadata": req.metadata or {},
        "indexed_at": datetime.now().isoformat()
    }
    document_store.append(doc)

    return {
        "doc_id": doc_id,
        "title": req.title,
        "chunks_count": len(chunks),
        "total_chars": len(req.content),
        "message": f"文档已索引，共 {len(chunks)} 个文本块"
    }


@app.get("/documents")
async def list_documents():
    """列出已索引的文档"""
    return {
        "total": len(document_store),
        "documents": [
            {"id": d["id"], "title": d["title"], "chunks": len(d["chunks"]),
             "doc_type": d["doc_type"], "indexed_at": d["indexed_at"]}
            for d in document_store
        ]
    }


@app.delete("/documents/{doc_id}")
async def delete_document(doc_id: str):
    """删除已索引的文档"""
    global document_store
    before = len(document_store)
    document_store = [d for d in document_store if d["id"] != doc_id]
    if len(document_store) == before:
        raise HTTPException(status_code=404, detail="文档不存在")
    return {"message": "文档已删除"}


# ── 智能问答 ──

@app.post("/ask")
async def ask_question(req: AskRequest):
    """
    RAG 智能问答
    1. 检索相关文档片段
    2. 拼接上下文
    3. 调用 LLM 生成回答
    """
    # 检索
    relevant_chunks = simple_search(req.question, req.top_k)

    if not relevant_chunks:
        return {
            "answer": "抱歉，没有找到与您问题相关的文档内容。请先上传相关课件或教材。",
            "sources": [],
            "has_context": False
        }

    # 拼接上下文
    context = "\n\n".join([
        f"【来源：{c['doc_title']}】\n{c['content']}"
        for c in relevant_chunks
    ])

    # LLM 回答
    system_prompt = """你是一个智能教育助手。请根据提供的参考文档内容回答学生的问题。
要求：
1. 基于文档内容回答，不要编造
2. 如果文档中没有相关信息，如实告知
3. 回答要清晰、有条理
4. 在回答末尾注明参考来源"""

    user_prompt = f"""参考文档内容：
{context}

学生问题：{req.question}

请根据以上文档内容回答："""

    answer = call_llm(user_prompt, system_prompt)

    return {
        "answer": answer,
        "sources": [
            {"doc_title": c["doc_title"], "chunk_preview": c["content"][:100], "score": c["score"]}
            for c in relevant_chunks
        ],
        "has_context": True
    }


# ── 知识图谱 ──

@app.post("/kg/node")
async def add_kg_node(req: KgNodeRequest):
    """添加知识图谱节点"""
    node_id = str(uuid.uuid4())[:8]
    node = {
        "id": node_id,
        "label": req.label,
        "type": req.node_type,
        "properties": req.properties or {}
    }
    knowledge_graph["nodes"].append(node)
    return node


@app.post("/kg/edge")
async def add_kg_edge(req: KgEdgeRequest):
    """添加知识图谱边（关系）"""
    edge = {
        "source": req.source_id,
        "target": req.target_id,
        "relation": req.relation
    }
    knowledge_graph["edges"].append(edge)
    return edge


@app.get("/kg")
async def get_knowledge_graph():
    """获取完整知识图谱"""
    return {
        "nodes": len(knowledge_graph["nodes"]),
        "edges": len(knowledge_graph["edges"]),
        "graph": knowledge_graph
    }


@app.get("/kg/node/{node_id}/related")
async def get_related_nodes(node_id: str):
    """查询节点的关联节点"""
    related = []
    for edge in knowledge_graph["edges"]:
        if edge["source"] == node_id:
            target = next((n for n in knowledge_graph["nodes"] if n["id"] == edge["target"]), None)
            if target:
                related.append({"node": target, "relation": edge["relation"], "direction": "outgoing"})
        elif edge["target"] == node_id:
            source = next((n for n in knowledge_graph["nodes"] if n["id"] == edge["source"]), None)
            if source:
                related.append({"node": source, "relation": edge["relation"], "direction": "incoming"})
    return {"node_id": node_id, "related": related}


# ── 知识点推荐 ──

@app.post("/recommend")
async def recommend_topics(req: RecommendRequest):
    """基于当前主题推荐相关知识点"""
    system_prompt = "你是知识点推荐助手。根据给定主题推荐相关的学习知识点，返回JSON数组。"
    user_prompt = f"""当前学习主题：{req.topic}
请推荐 {req.num_recommendations} 个相关的知识点，每个包含：
- title: 知识点名称
- reason: 推荐理由
- relevance: 相关度(0-1)

直接返回JSON数组："""

    result = call_llm(user_prompt, system_prompt)

    try:
        if "[" in result:
            start = result.index("[")
            end = result.rindex("]") + 1
            recommendations = json.loads(result[start:end])
        else:
            recommendations = json.loads(result)
    except (json.JSONDecodeError, ValueError):
        recommendations = [
            {"title": f"与{req.topic}相关的知识点{i+1}", "reason": "系统推荐", "relevance": 0.8 - i*0.1}
            for i in range(req.num_recommendations)
        ]

    return {
        "topic": req.topic,
        "recommendations": recommendations,
        "total": len(recommendations)
    }
