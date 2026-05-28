package com.autodev.course.controller;

import com.autodev.course.common.Result;
import com.autodev.course.dto.CourseDTO;
import com.autodev.course.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/course")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    /** 创建课程（教师） */
    @PostMapping
    public Result<CourseDTO> createCourse(@RequestBody CourseDTO dto,
                                          @RequestHeader("X-User-Id") Long teacherId,
                                          @RequestHeader("X-User-Name") String teacherName) {
        return Result.success(courseService.createCourse(dto, teacherId, teacherName));
    }

    /** 更新课程 */
    @PutMapping("/{id}")
    public Result<CourseDTO> updateCourse(@PathVariable Long id, @RequestBody CourseDTO dto) {
        return Result.success(courseService.updateCourse(id, dto));
    }

    /** 删除课程 */
    @DeleteMapping("/{id}")
    public Result<Void> deleteCourse(@PathVariable Long id) {
        courseService.deleteCourse(id);
        return Result.success();
    }

    /** 获取课程详情 */
    @GetMapping("/{id}")
    public Result<CourseDTO> getCourse(@PathVariable Long id) {
        return Result.success(courseService.getCourseById(id));
    }

    /** 获取所有已发布课程 */
    @GetMapping
    public Result<List<CourseDTO>> getAllCourses() {
        return Result.success(courseService.getAllCourses());
    }

    /** 获取教师的课程 */
    @GetMapping("/teacher/{teacherId}")
    public Result<List<CourseDTO>> getCoursesByTeacher(@PathVariable Long teacherId) {
        return Result.success(courseService.getCoursesByTeacher(teacherId));
    }

    /** 搜索课程 */
    @GetMapping("/search")
    public Result<List<CourseDTO>> searchCourses(@RequestParam String keyword) {
        return Result.success(courseService.searchCourses(keyword));
    }

    /** 发布课程 */
    @PutMapping("/{id}/publish")
    public Result<CourseDTO> publishCourse(@PathVariable Long id) {
        return Result.success(courseService.publishCourse(id));
    }

    /** 关闭课程 */
    @PutMapping("/{id}/close")
    public Result<CourseDTO> closeCourse(@PathVariable Long id) {
        return Result.success(courseService.closeCourse(id));
    }
}
