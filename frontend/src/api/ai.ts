import api from "./index";

export const aiApi = {
  // 笔记生成
  transcribe: (formData: FormData) =>
    api.post("/api/ai/note/transcribe", formData, {
      headers: { "Content-Type": "multipart/form-data" },
    }),
  generateNote: (params: { text: string; title?: string; courseName?: string }) =>
    api.post("/api/ai/note/generate", params),
  generateMindMap: (params: { content: string; title?: string }) =>
    api.post("/api/ai/note/mindmap", params),
  generateQuiz: (params: { content: string; numQuestions?: number }) =>
    api.post("/api/ai/note/quiz", params),

  // RAG 问答
  askQuestion: (params: { question: string; courseId?: string }) =>
    api.post("/api/ai/rag/ask", params),

  // 学情分析
  analyzeClassroom: (params: { text: string; courseName?: string }) =>
    api.post("/api/ai/analysis/classroom", params),
};
