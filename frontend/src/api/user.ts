import api from "./index";

export interface LoginParams {
  username: string;
  password: string;
}

export interface RegisterParams {
  username: string;
  password: string;
  email?: string;
  phone?: string;
  realName?: string;
  role?: string;
}

export const userApi = {
  login: (params: LoginParams) => api.post("/api/user/login", params),
  register: (params: RegisterParams) => api.post("/api/user/register", params),
  logout: () => api.post("/api/user/logout"),
  getCurrentUser: () => api.get("/api/user/me"),
  getUserById: (id: number) => api.get(`/api/user/${id}`),
  updateUser: (id: number, data: any) => api.put(`/api/user/${id}`, data),
};
