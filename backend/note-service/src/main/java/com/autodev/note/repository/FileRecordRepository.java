package com.autodev.note.repository;

import com.autodev.note.entity.FileRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FileRecordRepository extends JpaRepository<FileRecord, Long> {

    List<FileRecord> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<FileRecord> findByUserIdAndFileType(Long userId, FileRecord.FileType fileType);

    List<FileRecord> findByCourseId(Long courseId);
}
