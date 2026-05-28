package com.autodev.note.service;

import com.autodev.note.dto.NoteDTO;
import java.util.List;

public interface NoteService {

    NoteDTO createNote(NoteDTO dto);

    NoteDTO updateNote(Long id, NoteDTO dto);

    void deleteNote(Long id);

    NoteDTO getNoteById(Long id);

    List<NoteDTO> getNotesByUser(Long userId);

    List<NoteDTO> getNotesByCourseAndUser(Long courseId, Long userId);

    List<NoteDTO> searchNotes(Long userId, String keyword);

    /** 生成分享码并公开笔记 */
    NoteDTO shareNote(Long id);

    /** 取消分享 */
    NoteDTO unshareNote(Long id);

    /** 通过分享码访问笔记 */
    NoteDTO getNoteByShareCode(String shareCode);

    /** 获取公开分享的笔记列表 */
    List<NoteDTO> getSharedNotes();
}
