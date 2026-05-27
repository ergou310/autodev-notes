# AutoDev Notes 开发指南

## 项目概述

这是一个完整的智能教育助教平台毕设项目，采用 Java + Python 混合架构，微服务设计。

## 项目结构

```
autodev-notes/
├── frontend/                      # React 前端
├── backend/                       # Java 后端（微服务）
│   ├── user-service/              # 用户服务
│   ├── course-service/            # 课程服务
│   ├── note-service/              # 笔记服务
│   ├── ai-gateway/                # AI 网关
│   └── gateway/                   # API 网关
├── ai-service/                    # Python AI 服务
│   ├── note-generator/            # 笔记生成
│   ├── analysis-service/          # 学情分析
│   ├── grading-service/           # 智能阅卷
│   └── rag-service/               # RAG 检索
├── docker/                        # Docker 配置
├── docs/                          # 文档
└── README.md
```

## 技术栈详解

### 前端技术栈
- React 18 + TypeScript
- Ant Design 5.x（UI 组件库）
- React Router 6（路由）
- Axios（HTTP 客户端）
- Mind Elixir（思维导图）
- ECharts（数据可视化）

### Java 后端技术栈
- Spring Boot 3.2
- Spring Cloud Alibaba（Nacos 服务注册）
- Spring Security + Sa-Token（权限认证）
- Spring Data JPA（ORM）
- PostgreSQL（关系型数据库）
- Redis（缓存）
- Elasticsearch（全文检索）
- RabbitMQ（消息队列）
- MinIO（对象存储）

### Python AI 技术栈
- FastAPI（Web 框架）
- LangChain（AI 应用开发）
- OpenAI / Claude API（大模型）
- Whisper（语音转文字）
- PaddleOCR（文字识别）
- Neo4j（知识图谱）
- Chroma（向量数据库）
- Celery（异步任务）

## 开发环境准备

### 必需软件
- JDK 17+
- Node.js 18+
- Python 3.10+
- Maven 3.8+
- Docker + Docker Compose
- IDE（IntelliJ IDEA / VS Code）

### 启动基础设施

```bash
cd docker
docker-compose up -d
```

这会启动：
- PostgreSQL:5432
- Redis:6379
- RabbitMQ:5672 (管理界面:15672)
- MinIO:9000 (管理界面:9001)
- Neo4j:7474
- Elasticsearch:9200
- Nacos:8848

## 开发计划（12周）

### 第1-2周：项目初始化与用户服务
- [ ] 搭建开发环境
- [ ] 完成用户服务 CRUD
- [ ] 实现用户注册、登录、权限控制
- [ ] 前端登录/注册页面

### 第3-4周：课程服务与笔记服务
- [ ] 课程管理功能
- [ ] 文件上传（MinIO）
- [ ] 笔记管理
- [ ] 前端课程/笔记页面

### 第5-6周：AI 服务 - 笔记生成
- [ ] 语音转文字（Whisper）
- [ ] 笔记生成（LLM）
- [ ] 思维导图生成
- [ ] 前后端集成

### 第7-8周：AI 服务 - 高级功能
- [ ] RAG 检索系统
- [ ] 知识图谱（Neo4j）
- [ ] 学情分析
- [ ] 智能阅卷

### 第9-10周：微服务与网关
- [ ] API 网关
- [ ] 服务间调用
- [ ] 缓存优化
- [ ] 全文检索（Elasticsearch）

### 第11-12周：测试、优化与论文
- [ ] 系统测试
- [ ] 性能优化
- [ ] 论文撰写
- [ ] 答辩准备

## 核心功能详解

### 1. 用户服务（user-service）
- 用户注册/登录
- 角色管理（学生/教师/管理员）
- 个人信息管理
- 权限认证（Sa-Token）

### 2. 课程服务（course-service）
- 课程 CRUD
- 班级管理
- 学生选课
- 教学计划管理

### 3. 笔记服务（note-service）
- 文件上传（录音/PPT/视频）
- 笔记管理
- 笔记分享
- 笔记检索

### 4. AI 网关（ai-gateway）
- 路由到 Python AI 服务
- 请求聚合
- 异步任务管理

### 5. 笔记生成服务（note-generator）
- 语音转文字
- PPT 文本提取
- 笔记结构化生成
- 思维导图生成
- 复习题生成

### 6. 学情分析服务（analysis-service）
- 课堂质量分析
- 学生知识点掌握情况
- 班级数据统计
- 可视化报告

### 7. 智能阅卷服务（grading-service）
- 主观题自动批改
- 评分标准配置
- 成绩统计

### 8. RAG 检索服务（rag-service）
- 教材/课件向量化
- 智能问答
- 知识点推荐
- 学习路径规划

## 数据库设计（核心表）

### users（用户表）
| 字段 | 类型 | 说明 |
|------|------|------|
| id | bigint | 主键 |
| username | varchar | 用户名 |
| password | varchar | 密码（加密） |
| email | varchar | 邮箱 |
| phone | varchar | 手机号 |
| role | enum | 角色 |
| created_at | timestamp | 创建时间 |

### courses（课程表）
| 字段 | 类型 | 说明 |
|------|------|------|
| id | bigint | 主键 |
| name | varchar | 课程名 |
| description | text | 描述 |
| teacher_id | bigint | 教师ID |
| created_at | timestamp | 创建时间 |

### notes（笔记表）
| 字段 | 类型 | 说明 |
|------|------|------|
| id | bigint | 主键 |
| title | varchar | 标题 |
| content | text | 笔记内容 |
| mind_map | text | 思维导图（JSON） |
| quiz_questions | text | 复习题（JSON） |
| course_id | bigint | 课程ID |
| user_id | bigint | 用户ID |
| status | enum | 状态 |
| created_at | timestamp | 创建时间 |

## 部署说明

### 本地开发
1. 启动 Docker 基础设施
2. 启动后端微服务
3. 启动 Python AI 服务
4. 启动前端

### 生产部署
- 使用 Docker Compose 一键部署
- Nginx 反向代理
- 配置 HTTPS

## 答辩演示要点

1. 上传课堂录音，演示自动生成笔记
2. 展示思维导图和复习题
3. 演示 AI 问答功能
4. 展示教师端学情分析
5. 演示智能阅卷功能

## 注意事项

- API 文档使用 Swagger/OpenAPI
- 代码注释要完整
- 提交记录要规范
- 文档要及时更新
