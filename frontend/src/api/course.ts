import api from "./index";

export const courseApi = {
  getAll: () => api.get("/api/course"),
  getById: (id: number) => api.get(`/api/course/${id}`),
  create: (data: any) => api.post("/api/course", data),
  update: (id: number, data: any) => api.put(`/api/course/${id}`, data),
  delete: (id: number) => api.delete(`/api/course/${id}`),
  search: (keyword: string) => api.get(`/api/course/search?keyword=${keyword}`),
  publish: (id: number) => api.put(`/api/course/${id}/publish`),
};

export const enrollApi = {
  enroll: (studentId: number, courseId: number, classroomId: number) =>
    api.post("/api/enroll", { studentId, courseId, classroomId }),
  drop: (studentId: number, courseId: number) =>
    api.delete(`/api/enroll?studentId=${studentId}&courseId=${courseId}`),
  getEnrolledCourses: (studentId: number) =>
    api.get(`/api/enroll/student/${studentId}/courses`),
  getClassrooms: (courseId: number) =>
    api.get(`/api/enroll/course/${courseId}/classrooms`),
};
