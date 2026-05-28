# AutoDev Notes 开发执行计划

## 开发顺序（共10个阶段）

### 阶段1：用户服务 [~40min]
- [ ] application.yml 配置
- [ ] User 实体类完善（字段、关联）
- [ ] UserRepository
- [ ] UserService（注册、登录、CRUD）
- [ ] UserController（REST API）
- [ ] Sa-Token 认证配置
- [ ] 全局异常处理
- [ ] 统一响应格式

### 阶段2：课程服务 [~30min]
- [ ] Course 实体类
- [ ] 班级 ClassRoom 实体类
- [ ] CourseRepository + Service + Controller
- [ ] 学生选课功能
- [ ] 课程与用户关联

### 阶段3：笔记服务 [~30min]
- [ ] Note 实体类（标题、内容、思维导图JSON、复习题JSON）
- [ ] NoteRepository + Service + Controller
- [ ] 文件上传接口（录音/PPT/视频）
- [ ] 笔记与课程、用户关联
- [ ] 笔记分享功能

### 阶段4：AI网关 [~20min]
- [ ] 路由到 Python AI 服务
- [ ] 异步任务管理（任务状态查询）
- [ ] 请求/响应转换

### 阶段5：API网关 [~20min]
- [ ] Spring Cloud Gateway 配置
- [ ] 路由规则
- [ ] 跨域配置
- [ ] 统一鉴权过滤器

### 阶段6：AI笔记生成服务（Python）[~40min]
- [ ] FastAPI 应用结构
- [ ] 语音转文字接口（Whisper）
- [ ] 笔记生成接口（LLM）
- [ ] 思维导图生成接口
- [ ] 复习题生成接口
- [ ] 异步任务处理

### 阶段7：RAG检索服务（Python）[~40min]
- [ ] 向量化接口（Chroma）
- [ ] 智能问答接口
- [ ] 知识图谱接口（Neo4j）
- [ ] 知识点推荐

### 阶段8：学情分析+智能阅卷（Python）[~30min]
- [ ] 课堂质量分析接口
- [ ] 学生知识点掌握分析
- [ ] 主观题自动批改
- [ ] 成绩统计

### 阶段9：前端（React）[~60min]
- [ ] 项目初始化 + 路由配置
- [ ] 登录/注册页面
- [ ] 课程列表/详情页面
- [ ] 笔记管理页面
- [ ] AI笔记生成页面（上传录音→生成笔记）
- [ ] 思维导图展示组件
- [ ] 学情分析可视化页面（ECharts）
- [ ] 智能问答页面

### 阶段10：Docker部署 + 联调 [~20min]
- [ ] 各服务 Dockerfile
- [ ] docker-compose 完善
- [ ] Nginx 反向代理配置
- [ ] 启动测试

## 预计总时间：~5小时
## 状态：进行中
