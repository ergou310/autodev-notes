package com.autodev.note.service.impl;

import com.autodev.note.dto.NoteDTO;
import com.autodev.note.entity.Note;
import com.autodev.note.repository.NoteRepository;
import com.autodev.note.service.NoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NoteServiceImpl implements NoteService {

    private final NoteRepository noteRepository;

    @Override
    @Transactional
    public NoteDTO createNote(NoteDTO dto) {
        Note note = new Note();
        note.setTitle(dto.getTitle());
        note.setContent(dto.getContent());
        note.setMindMap(dto.getMindMap());
        note.setQuizQuestions(dto.getQuizQuestions());
        note.setCourseId(dto.getCourseId());
        note.setUserId(dto.getUserId());
        note.setSource(dto.getSource() != null ? Note.NoteSource.valueOf(dto.getSource()) : Note.NoteSource.MANUAL);
        note.setSourceFileId(dto.getSourceFileId());
        note.setTags(dto.getTags());
        note = noteRepository.save(note);
        return NoteDTO.fromEntity(note);
    }

    @Override
    @Transactional
    public NoteDTO updateNote(Long id, NoteDTO dto) {
        Note note = noteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("笔记不存在"));
        if (dto.getTitle() != null) note.setTitle(dto.getTitle());
        if (dto.getContent() != null) note.setContent(dto.getContent());
        if (dto.getMindMap() != null) note.setMindMap(dto.getMindMap());
        if (dto.getQuizQuestions() != null) note.setQuizQuestions(dto.getQuizQuestions());
        if (dto.getTags() != null) note.setTags(dto.getTags());
        if (dto.getStatus() != null) note.setStatus(Note.NoteStatus.valueOf(dto.getStatus()));
        note = noteRepository.save(note);
        return NoteDTO.fromEntity(note);
    }

    @Override
    @Transactional
    public void deleteNote(Long id) {
        if (!noteRepository.existsById(id)) {
            throw new RuntimeException("笔记不存在");
        }
        noteRepository.deleteById(id);
    }

    @Override
    public NoteDTO getNoteById(Long id) {
        Note note = noteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("笔记不存在"));
        return NoteDTO.fromEntity(note);
    }

    @Override
    public List<NoteDTO> getNotesByUser(Long userId) {
        return noteRepository.findByUserIdOrderByUpdatedAtDesc(userId).stream()
                .map(NoteDTO::fromEntity).collect(Collectors.toList());
    }

    @Override
    public List<NoteDTO> getNotesByCourseAndUser(Long courseId, Long userId) {
        return noteRepository.findByCourseIdAndUserId(courseId, userId).stream()
                .map(NoteDTO::fromEntity).collect(Collectors.toList());
    }

    @Override
    public List<NoteDTO> searchNotes(Long userId, String keyword) {
        return noteRepository.searchByKeyword(userId, keyword).stream()
                .map(NoteDTO::fromEntity).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public NoteDTO shareNote(Long id) {
        Note note = noteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("笔记不存在"));
        note.setShared(true);
        note.setShareCode(UUID.randomUUID().toString().replace("-", ""));
        return NoteDTO.fromEntity(noteRepository.save(note));
    }

    @Override
    @Transactional
    public NoteDTO unshareNote(Long id) {
        Note note = noteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("笔记不存在"));
        note.setShared(false);
        note.setShareCode(null);
        return NoteDTO.fromEntity(noteRepository.save(note));
    }

    @Override
    public NoteDTO getNoteByShareCode(String shareCode) {
        Note note = noteRepository.findByShareCode(shareCode)
                .orElseThrow(() -> new RuntimeException("分享链接无效或已过期"));
        return NoteDTO.fromEntity(note);
    }

    @Override
    public List<NoteDTO> getSharedNotes() {
        return noteRepository.findBySharedTrueOrderByCreatedAtDesc().stream()
                .map(NoteDTO::fromEntity).collect(Collectors.toList());
    }
}
