package com.autodev.course.service;

import com.autodev.course.dto.ClassRoomDTO;
import java.util.List;

public interface EnrollService {

    /** 学生选课 */
    void enroll(Long studentId, Long courseId, Long classroomId);

    /** 学生退课 */
    void drop(Long studentId, Long courseId);

    /** 查询学生已选课程ID列表 */
    List<Long> getEnrolledCourseIds(Long studentId);

    /** 查询课程已选学生ID列表 */
    List<Long> getEnrolledStudentIds(Long courseId);

    /** 创建班级 */
    ClassRoomDTO createClassRoom(ClassRoomDTO dto);

    /** 查询课程下的班级 */
    List<ClassRoomDTO> getClassRoomsByCourse(Long courseId);
}
