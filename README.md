# AutoDev Notes - 智能教育助教平台

> 一个基于 Java + Python 的智能教育助教系统，涵盖"教-学-练-评"全流程

## 项目简介

AutoDev Notes 是一个面向高校师生的智能教育助教平台，提供以下功能：

### 教师端
- 📊 课堂质量分析（录音分析、授课建议）
- 👥 学生学情分析（知识点掌握情况、班级对比）
- 📝 智能阅卷（主观题自动批改）
- 📚 教案生成助手
- 📈 教学数据可视化

### 学生端
- 🎤 多模态课堂笔记生成（录音/PPT/视频）
- 🧠 思维导图自动生成
- ❓ 复习题自动生成
- 🤖 AI 辅导老师（智能问答）
- 📖 学习路径规划
- 🎯 错题本系统

## 技术架构

### 前端
- React 18 + TypeScript
- Ant Design
- Mind Elixir（思维导图）
- ECharts（数据可视化）

### Java 后端（微服务架构）
- Spring Boot 3.2
- Spring Cloud Alibaba（服务治理）
- Spring Security + OAuth2.0
- PostgreSQL（关系型数据）
- Redis（缓存）
- Elasticsearch（全文检索）
- RabbitMQ（消息队列）
- MinIO（对象存储）

### Python AI 服务
- FastAPI
- LangChain
- OpenAI / Claude API
- Whisper（语音转文字）
- PaddleOCR（文字识别）
- Neo4j（知识图谱）
- Chroma（向量数据库）

## 项目结构

```
autodev-notes/
├── frontend/                 # React 前端
├── backend/                  # Java 微服务后端
│   ├── user-service/         # 用户服务
│   ├── course-service/       # 课程服务
│   ├── note-service/         # 笔记服务
│   ├── ai-gateway/           # AI 网关服务
│   └── gateway/              # API 网关
├── ai-service/               # Python AI 服务
│   ├── note-generator/       # 笔记生成服务
│   ├── analysis-service/     # 学情分析服务
│   ├── grading-service/      # 智能阅卷服务
│   └── rag-service/          # RAG 检索服务
├── docker/                   # Docker 配置
└── docs/                     # 文档
```

## 快速开始

详见各子目录的 README.md

## 开发计划

- 第1-4周：微服务架构搭建、核心业务模块
- 第5-8周：AI 深度功能开发、知识图谱
- 第9-12周：系统优化、测试、论文

## License

MIT
