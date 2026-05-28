package com.autodev.course.controller;

import com.autodev.course.common.Result;
import com.autodev.course.dto.ClassRoomDTO;
import com.autodev.course.service.EnrollService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/enroll")
@RequiredArgsConstructor
public class EnrollController {

    private final EnrollService enrollService;

    /** 学生选课 */
    @PostMapping
    public Result<Void> enroll(@RequestBody Map<String, Long> body) {
        enrollService.enroll(body.get("studentId"), body.get("courseId"), body.get("classroomId"));
        return Result.success();
    }

    /** 学生退课 */
    @DeleteMapping
    public Result<Void> drop(@RequestParam Long studentId, @RequestParam Long courseId) {
        enrollService.drop(studentId, courseId);
        return Result.success();
    }

    /** 查询学生已选课程 */
    @GetMapping("/student/{studentId}/courses")
    public Result<List<Long>> getEnrolledCourses(@PathVariable Long studentId) {
        return Result.success(enrollService.getEnrolledCourseIds(studentId));
    }

    /** 查询课程已选学生 */
    @GetMapping("/course/{courseId}/students")
    public Result<List<Long>> getEnrolledStudents(@PathVariable Long courseId) {
        return Result.success(enrollService.getEnrolledStudentIds(courseId));
    }

    /** 创建班级 */
    @PostMapping("/classroom")
    public Result<ClassRoomDTO> createClassRoom(@RequestBody ClassRoomDTO dto) {
        return Result.success(enrollService.createClassRoom(dto));
    }

    /** 查询课程下的班级 */
    @GetMapping("/course/{courseId}/classrooms")
    public Result<List<ClassRoomDTO>> getClassRooms(@PathVariable Long courseId) {
        return Result.success(enrollService.getClassRoomsByCourse(courseId));
    }
}
