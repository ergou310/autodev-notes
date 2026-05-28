package com.autodev.note.repository;

import com.autodev.note.entity.Note;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface NoteRepository extends JpaRepository<Note, Long> {

    List<Note> findByUserIdOrderByUpdatedAtDesc(Long userId);

    List<Note> findByCourseIdAndUserId(Long courseId, Long userId);

    List<Note> findBySharedTrueOrderByCreatedAtDesc();

    Optional<Note> findByShareCode(String shareCode);

    @Query("SELECT n FROM Note n WHERE n.userId = :userId " +
           "AND (LOWER(n.title) LIKE LOWER(CONCAT('%',:keyword,'%')) " +
           "OR LOWER(n.content) LIKE LOWER(CONCAT('%',:keyword,'%')))")
    List<Note> searchByKeyword(@Param("userId") Long userId, @Param("keyword") String keyword);

    List<Note> findByUserIdAndTagsContaining(Long userId, String tag);
}
