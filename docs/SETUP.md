# AutoDev Notes — 项目部署与操作指南

> 拉取代码后，请按本文档顺序操作。

---

## 一、环境准备

### 1.1 必需软件

| 软件 | 版本要求 | 用途 |
|------|---------|------|
| JDK | 17+ | Java 后端 |
| Maven | 3.8+ | Java 构建 |
| Node.js | 18+ | 前端 |
| Python | 3.10+ | AI 服务 |
| Docker + Docker Compose | 最新 | 基础设施 + 部署 |

### 1.2 安装检查

```bash
java -version
mvn -version
node -v
npm -v
python3 --version
docker -v
docker compose version
```

---

## 二、拉取代码

```bash
git clone https://github.com/ergou310/autodev-notes.git
cd autodev-notes
```

---

## 三、启动基础设施（Docker）

```bash
cd docker
docker compose up -d
```

启动的服务：

| 服务 | 端口 | 管理界面 |
|------|------|---------|
| PostgreSQL | 5432 | — |
| Redis | 6379 | — |
| RabbitMQ | 5672 | http://localhost:15672 (autodev/autodev123) |
| MinIO | 9000 | http://localhost:9001 (autodev/autodev123) |
| Neo4j | 7687 | http://localhost:7474 (neo4j/autodev123) |
| Elasticsearch | 9200 | — |
| Nacos | 8848 | http://localhost:8848/nacos |

**验证**：
```bash
docker compose ps
# 确认所有容器状态为 running
```

---

## 四、配置 API Key（重要）

AI 服务需要大模型 API Key，不配置也能跑（返回模拟数据），但答辩演示建议配置。

### 4.1 创建环境变量文件

```bash
cd ai-service
cp .env.example .env
```

### 4.2 编辑 .env，填入 API Key

推荐用 DeepSeek（便宜）：

```
DEEPSEEK_API_KEY=你的Key
DEEPSEEK_BASE_URL=https://api.deepseek.com/v1
LLM_MODEL=deepseek-chat
```

或用 OpenAI：

```
OPENAI_API_KEY=你的Key
OPENAI_BASE_URL=https://api.openai.com/v1
LLM_MODEL=gpt-4o-mini
```

> **没有 API Key 也能运行**，AI 接口会返回模拟数据，适合开发调试。

---

## 五、启动后端服务

### 5.1 Java 后端（4个服务，分别开4个终端）

**终端1 — 用户服务 (port 8081)**：
```bash
cd backend/user-service
mvn clean package -DskipTests
java -jar target/user-service-1.0.0.jar
```

**终端2 — 课程服务 (port 8082)**：
```bash
cd backend/course-service
mvn clean package -DskipTests
java -jar target/course-service-1.0.0.jar
```

**终端3 — 笔记服务 (port 8083)**：
```bash
cd backend/note-service
mvn clean package -DskipTests
java -jar target/note-service-1.0.0.jar
```

**终端4 — AI网关 (port 8084)**：
```bash
cd backend/ai-gateway
mvn clean package -DskipTests
java -jar target/ai-gateway-1.0.0.jar
```

**终端5 — API网关 (port 8080)**：
```bash
cd backend/gateway
mvn clean package -DskipTests
java -jar target/gateway-1.0.0.jar
```

### 5.2 Python AI 服务（4个服务，分别开4个终端）

先安装依赖（每个服务目录下）：
```bash
cd ai-service
pip install -r requirements.txt
```

**终端6 — 笔记生成服务 (port 8091)**：
```bash
cd ai-service/note-generator
uvicorn main:app --host 0.0.0.0 --port 8091 --reload
```

**终端7 — RAG检索服务 (port 8092)**：
```bash
cd ai-service/rag-service
uvicorn main:app --host 0.0.0.0 --port 8092 --reload
```

**终端8 — 学情分析服务 (port 8093)**：
```bash
cd ai-service/analysis-service
uvicorn main:app --host 0.0.0.0 --port 8093 --reload
```

**终端9 — 智能阅卷服务 (port 8094)**：
```bash
cd ai-service/grading-service
uvicorn main:app --host 0.0.0.0 --port 8094 --reload
```

### 5.3 验证后端服务

```bash
# API 网关
curl http://localhost:8080/

# 用户服务（通过网关）
curl http://localhost:8080/api/user/me

# AI 笔记生成
curl http://localhost:8091/health

# RAG 服务
curl http://localhost:8092/health

# 学情分析
curl http://localhost:8093/health

# 智能阅卷
curl http://localhost:8094/health
```

---

## 六、启动前端

```bash
cd frontend
npm install
npm start
```

前端默认运行在 http://localhost:3000

> 如果端口冲突，可用 `PORT=3001 npm start`

---

## 七、端口汇总

| 服务 | 端口 |
|------|------|
| **API 网关** | **8080** |
| 用户服务 | 8081 |
| 课程服务 | 8082 |
| 笔记服务 | 8083 |
| AI 网关 | 8084 |
| 笔记生成 (Python) | 8091 |
| RAG 检索 (Python) | 8092 |
| 学情分析 (Python) | 8093 |
| 智能阅卷 (Python) | 8094 |
| **前端** | **3000** |
| PostgreSQL | 5432 |
| Redis | 6379 |
| MinIO | 9000/9001 |
| Neo4j | 7474 |
| Elasticsearch | 9200 |
| Nacos | 8848 |

---

## 八、阶段10：Docker 部署（生产方案）

以下文件均已在 `docker/` 目录下，需要补充各服务的 Dockerfile 和 nginx 配置。

### 8.1 各服务 Dockerfile

**Java 服务通用 Dockerfile**（每个 Java 服务目录下创建 `Dockerfile`）：

```dockerfile
# backend/user-service/Dockerfile（其他 Java 服务同理）
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests -B

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
```

> 为每个 Java 服务创建同样的 Dockerfile，只改 EXPOSE 端口号。

**Python 服务通用 Dockerfile**：

```dockerfile
# ai-service/note-generator/Dockerfile（其他 Python 服务同理）
FROM python:3.11-slim
WORKDIR /app
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt
COPY . .
EXPOSE 8091
CMD ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "8091"]
```

**前端 Dockerfile**：

```dockerfile
# frontend/Dockerfile
FROM node:18-alpine AS build
WORKDIR /app
COPY package.json package-lock.json* ./
RUN npm install
COPY . .
RUN npm run build

FROM nginx:alpine
COPY --from=build /app/build /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
```

**前端 nginx.conf**（`frontend/nginx.conf`）：

```nginx
server {
    listen 80;
    server_name localhost;

    location / {
        root /usr/share/nginx/html;
        index index.html;
        try_files $uri $uri/ /index.html;
    }

    location /api/ {
        proxy_pass http://gateway:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

### 8.2 完整 docker-compose.yml

在项目根目录创建 `docker-compose.prod.yml`：

```yaml
version: '3.8'

services:
  # ── 基础设施 ──
  postgres:
    image: postgres:16-alpine
    environment:
      POSTGRES_DB: autodev_notes
      POSTGRES_USER: autodev
      POSTGRES_PASSWORD: autodev123
    volumes:
      - postgres_data:/var/lib/postgresql/data
    networks:
      - autodev-net

  redis:
    image: redis:7-alpine
    volumes:
      - redis_data:/data
    networks:
      - autodev-net

  minio:
    image: minio/minio:latest
    command: server /data --console-address ":9001"
    environment:
      MINIO_ROOT_USER: autodev
      MINIO_ROOT_PASSWORD: autodev123
    volumes:
      - minio_data:/data
    networks:
      - autodev-net

  # ── Java 后端 ──
  gateway:
    build: ./backend/gateway
    ports:
      - "8080:8080"
    depends_on: [user-service, course-service, note-service, ai-gateway]
    networks:
      - autodev-net

  user-service:
    build: ./backend/user-service
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/autodev_notes
      SPRING_DATASOURCE_USERNAME: autodev
      SPRING_DATASOURCE_PASSWORD: autodev123
    depends_on: [postgres]
    networks:
      - autodev-net

  course-service:
    build: ./backend/course-service
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/autodev_notes
      SPRING_DATASOURCE_USERNAME: autodev
      SPRING_DATASOURCE_PASSWORD: autodev123
    depends_on: [postgres]
    networks:
      - autodev-net

  note-service:
    build: ./backend/note-service
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/autodev_notes
      SPRING_DATASOURCE_USERNAME: autodev
      SPRING_DATASOURCE_PASSWORD: autodev123
      MINIO_ENDPOINT: http://minio:9000
    depends_on: [postgres, minio]
    networks:
      - autodev-net

  ai-gateway:
    build: ./backend/ai-gateway
    environment:
      SPRING_DATA_REDIS_HOST: redis
      AI_SERVICES_NOTE-GENERATOR: http://note-generator:8091
      AI_SERVICES_RAG-SERVICE: http://rag-service:8092
      AI_SERVICES_ANALYSIS-SERVICE: http://analysis-service:8093
      AI_SERVICES_GRADING-SERVICE: http://grading-service:8094
    depends_on: [redis, note-generator, rag-service, analysis-service, grading-service]
    networks:
      - autodev-net

  # ── Python AI 服务 ──
  note-generator:
    build: ./ai-service/note-generator
    environment:
      DEEPSEEK_API_KEY: ${DEEPSEEK_API_KEY}
      DEEPSEEK_BASE_URL: ${DEEPSEEK_BASE_URL:-https://api.deepseek.com/v1}
      LLM_MODEL: ${LLM_MODEL:-deepseek-chat}
    networks:
      - autodev-net

  rag-service:
    build: ./ai-service/rag-service
    environment:
      DEEPSEEK_API_KEY: ${DEEPSEEK_API_KEY}
      DEEPSEEK_BASE_URL: ${DEEPSEEK_BASE_URL:-https://api.deepseek.com/v1}
      LLM_MODEL: ${LLM_MODEL:-deepseek-chat}
    networks:
      - autodev-net

  analysis-service:
    build: ./ai-service/analysis-service
    environment:
      DEEPSEEK_API_KEY: ${DEEPSEEK_API_KEY}
      DEEPSEEK_BASE_URL: ${DEEPSEEK_BASE_URL:-https://api.deepseek.com/v1}
      LLM_MODEL: ${LLM_MODEL:-deepseek-chat}
    networks:
      - autodev-net

  grading-service:
    build: ./ai-service/grading-service
    environment:
      DEEPSEEK_API_KEY: ${DEEPSEEK_API_KEY}
      DEEPSEEK_BASE_URL: ${DEEPSEEK_BASE_URL:-https://api.deepseek.com/v1}
      LLM_MODEL: ${LLM_MODEL:-deepseek-chat}
    networks:
      - autodev-net

  # ── 前端 ──
  frontend:
    build: ./frontend
    ports:
      - "80:80"
    depends_on: [gateway]
    networks:
      - autodev-net

volumes:
  postgres_data:
  redis_data:
  minio_data:

networks:
  autodev-net:
    driver: bridge
```

### 8.3 一键部署命令

```bash
# 设置 API Key（可选）
export DEEPSEEK_API_KEY=你的Key

# 构建并启动所有服务
docker compose -f docker-compose.prod.yml up -d --build

# 查看运行状态
docker compose -f docker-compose.prod.yml ps

# 查看日志
docker compose -f docker-compose.prod.yml logs -f gateway

# 停止所有服务
docker compose -f docker-compose.prod.yml down
```

启动后访问 http://localhost 即可。

---

## 九、常见问题

### Q1: Java 服务启动报数据库连接失败
确认 PostgreSQL 已启动：`docker compose ps postgres`
确认数据库已创建：`docker exec -it autodev-postgres psql -U autodev -c "\\l"`

### Q2: 前端请求 404
确认 API 网关已启动：`curl http://localhost:8080/`
检查前端 `src/api/index.ts` 中的 baseURL 是否正确

### Q3: AI 接口返回模拟数据
正常现象，说明没有配置 API Key。配置 `.env` 后重启 Python 服务即可。

### Q4: MinIO 上传文件失败
确认 MinIO 已启动：访问 http://localhost:9001
首次需要手动创建 bucket：登录后创建名为 `autodev-notes` 的 bucket

### Q5: Maven 构建慢
在 `~/.m2/settings.xml` 中配置阿里云镜像：
```xml
<mirror>
  <id>aliyun</id>
  <mirrorOf>central</mirrorOf>
  <url>https://maven.aliyun.com/repository/central</url>
</mirror>
```

### Q6: npm install 慢
```bash
npm config set registry https://registry.npmmirror.com
npm install
```

---

## 十、API 接口速查

### 用户服务
| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/user/register | 注册 |
| POST | /api/user/login | 登录（返回 token） |
| POST | /api/user/logout | 退出 |
| GET | /api/user/me | 当前用户 |
| GET | /api/user/{id} | 用户详情 |
| PUT | /api/user/{id} | 更新用户 |

### 课程服务
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/course | 课程列表 |
| POST | /api/course | 创建课程 |
| GET | /api/course/{id} | 课程详情 |
| PUT | /api/course/{id} | 更新课程 |
| DELETE | /api/course/{id} | 删除课程 |
| GET | /api/course/search?keyword= | 搜索课程 |
| PUT | /api/course/{id}/publish | 发布课程 |
| POST | /api/enroll | 选课 |
| DELETE | /api/enroll | 退课 |

### 笔记服务
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/note/user/{userId} | 用户笔记列表 |
| POST | /api/note | 创建笔记 |
| GET | /api/note/{id} | 笔记详情 |
| PUT | /api/note/{id} | 更新笔记 |
| DELETE | /api/note/{id} | 删除笔记 |
| GET | /api/note/search?userId=&keyword= | 搜索笔记 |
| PUT | /api/note/{id}/share | 分享笔记 |
| GET | /api/note/share/{code} | 通过分享码访问 |
| POST | /api/file/upload | 上传文件 |
| GET | /api/file/download/{id} | 下载文件 |

### AI 服务
| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/ai/note/transcribe | 语音转文字 |
| POST | /api/ai/note/generate | 生成笔记 |
| POST | /api/ai/note/mindmap | 生成思维导图 |
| POST | /api/ai/note/quiz | 生成复习题 |
| POST | /api/ai/rag/ask | RAG 智能问答 |
| POST | /api/ai/analysis/classroom | 课堂质量分析 |
| POST | /api/ai/grading/grade | 自动批改 |

---

## 十一、答辩演示流程建议

1. **注册/登录** — 演示学生注册，教师注册，登录
2. **课程管理** — 教师创建课程，发布课程
3. **AI笔记生成** — 上传一段录音（或直接粘贴文本），展示自动生成的笔记、思维导图、复习题
4. **笔记管理** — 查看笔记列表，编辑笔记，分享笔记
5. **智能问答** — 提问一个课程相关问题，展示 RAG 回答
6. **学情分析** — 粘贴课堂文本，展示分析结果（评分、关键词、建议）
7. **架构讲解** — 展示微服务架构图、技术栈、Java+Python混合架构

> 提前准备一段中文课堂录音（5分钟左右），或准备一段课堂文本，方便现场演示。
