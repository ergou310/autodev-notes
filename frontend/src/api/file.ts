import api from "./index";

export const fileApi = {
  upload: (formData: FormData) =>
    api.post("/api/file/upload", formData, {
      headers: { "Content-Type": "multipart/form-data" },
    }),
  download: (id: number) => `/api/file/download/${id}`,
  delete: (id: number) => api.delete(`/api/file/${id}`),
};
