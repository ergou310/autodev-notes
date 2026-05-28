import axios from "axios";

const api = axios.create({
  baseURL: "http://localhost:8080",  // API 网关地址
  timeout: 30000,
});

// 请求拦截器：自动添加 Token
api.interceptors.request.use((config) => {
  const token = localStorage.getItem("token");
  if (token) {
    config.headers.Authorization = token;
  }
  return config;
});

// 响应拦截器：统一处理错误
api.interceptors.response.use(
  (response) => {
    const data = response.data;
    if (data.code && data.code !== 200) {
      return Promise.reject(new Error(data.message || "请求失败"));
    }
    return data;
  },
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem("token");
      localStorage.removeItem("user");
      window.location.href = "/login";
    }
    return Promise.reject(error);
  }
);

export default api;
