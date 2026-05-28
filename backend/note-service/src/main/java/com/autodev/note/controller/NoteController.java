package com.autodev.note.controller;

import com.autodev.note.common.Result;
import com.autodev.note.dto.NoteDTO;
import com.autodev.note.service.NoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/note")
@RequiredArgsConstructor
public class NoteController {

    private final NoteService noteService;

    /** 创建笔记 */
    @PostMapping
    public Result<NoteDTO> createNote(@RequestBody NoteDTO dto) {
        return Result.success(noteService.createNote(dto));
    }

    /** 更新笔记 */
    @PutMapping("/{id}")
    public Result<NoteDTO> updateNote(@PathVariable Long id, @RequestBody NoteDTO dto) {
        return Result.success(noteService.updateNote(id, dto));
    }

    /** 删除笔记 */
    @DeleteMapping("/{id}")
    public Result<Void> deleteNote(@PathVariable Long id) {
        noteService.deleteNote(id);
        return Result.success();
    }

    /** 获取笔记详情 */
    @GetMapping("/{id}")
    public Result<NoteDTO> getNote(@PathVariable Long id) {
        return Result.success(noteService.getNoteById(id));
    }

    /** 获取用户的所有笔记 */
    @GetMapping("/user/{userId}")
    public Result<List<NoteDTO>> getNotesByUser(@PathVariable Long userId) {
        return Result.success(noteService.getNotesByUser(userId));
    }

    /** 获取用户在某课程下的笔记 */
    @GetMapping("/user/{userId}/course/{courseId}")
    public Result<List<NoteDTO>> getNotesByCourseAndUser(@PathVariable Long userId,
                                                          @PathVariable Long courseId) {
        return Result.success(noteService.getNotesByCourseAndUser(courseId, userId));
    }

    /** 搜索笔记 */
    @GetMapping("/search")
    public Result<List<NoteDTO>> searchNotes(@RequestParam Long userId,
                                              @RequestParam String keyword) {
        return Result.success(noteService.searchNotes(userId, keyword));
    }

    /** 分享笔记 */
    @PutMapping("/{id}/share")
    public Result<NoteDTO> shareNote(@PathVariable Long id) {
        return Result.success(noteService.shareNote(id));
    }

    /** 取消分享 */
    @PutMapping("/{id}/unshare")
    public Result<NoteDTO> unshareNote(@PathVariable Long id) {
        return Result.success(noteService.unshareNote(id));
    }

    /** 通过分享码访问笔记（无需登录） */
    @GetMapping("/share/{shareCode}")
    public Result<NoteDTO> getNoteByShareCode(@PathVariable String shareCode) {
        return Result.success(noteService.getNoteByShareCode(shareCode));
    }

    /** 获取公开分享的笔记列表 */
    @GetMapping("/shared")
    public Result<List<NoteDTO>> getSharedNotes() {
        return Result.success(noteService.getSharedNotes());
    }
}
