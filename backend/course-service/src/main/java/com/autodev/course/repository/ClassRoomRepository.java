package com.autodev.course.repository;

import com.autodev.course.entity.ClassRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ClassRoomRepository extends JpaRepository<ClassRoom, Long> {

    List<ClassRoom> findByCourseId(Long courseId);
}
