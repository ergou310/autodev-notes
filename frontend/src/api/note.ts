import api from "./index";

export const noteApi = {
  create: (data: any) => api.post("/api/note", data),
  update: (id: number, data: any) => api.put(`/api/note/${id}`, data),
  delete: (id: number) => api.delete(`/api/note/${id}`),
  getById: (id: number) => api.get(`/api/note/${id}`),
  getByUser: (userId: number) => api.get(`/api/note/user/${userId}`),
  search: (userId: number, keyword: string) =>
    api.get(`/api/note/search?userId=${userId}&keyword=${keyword}`),
  share: (id: number) => api.put(`/api/note/${id}/share`),
  unshare: (id: number) => api.put(`/api/note/${id}/unshare`),
  getByShareCode: (code: string) => api.get(`/api/note/share/${code}`),
};
