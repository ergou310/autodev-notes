package com.autodev.note.service;

import com.autodev.note.dto.NoteDTO;
import com.autodev.note.entity.Note;
import com.autodev.note.repository.NoteRepository;
import com.autodev.note.service.impl.NoteServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NoteServiceImplTest {

    @Mock
    private NoteRepository noteRepository;

    @InjectMocks
    private NoteServiceImpl noteService;

    private Note sampleNote;

    @BeforeEach
    void setUp() {
        sampleNote = new Note();
        sampleNote.setId(1L);
        sampleNote.setTitle("My Note");
        sampleNote.setContent("Note content");
        sampleNote.setMindMap("{\"nodes\":[]}");
        sampleNote.setQuizQuestions("{\"questions\":[]}");
        sampleNote.setCourseId(10L);
        sampleNote.setUserId(1L);
        sampleNote.setSource(Note.NoteSource.MANUAL);
        sampleNote.setShared(false);
        sampleNote.setTags("java,spring");
        sampleNote.setStatus(Note.NoteStatus.DRAFT);
        sampleNote.setCreatedAt(LocalDateTime.now());
        sampleNote.setUpdatedAt(LocalDateTime.now());
    }

    @Nested
    @DisplayName("createNote")
    class CreateNote {

        @Test
        @DisplayName("should create note and return DTO")
        void createNote_success() {
            when(noteRepository.save(any(Note.class))).thenAnswer(invocation -> {
                Note n = invocation.getArgument(0);
                n.setId(1L);
                return n;
            });

            NoteDTO input = new NoteDTO();
            input.setTitle("My Note");
            input.setContent("Note content");
            input.setMindMap("{\"nodes\":[]}");
            input.setQuizQuestions("{\"questions\":[]}");
            input.setCourseId(10L);
            input.setUserId(1L);
            input.setTags("java,spring");

            NoteDTO result = noteService.createNote(input);

            assertThat(result).isNotNull();
            assertThat(result.getTitle()).isEqualTo("My Note");
            assertThat(result.getContent()).isEqualTo("Note content");
            assertThat(result.getUserId()).isEqualTo(1L);
            assertThat(result.getCourseId()).isEqualTo(10L);
            assertThat(result.getSource()).isEqualTo("MANUAL");

            verify(noteRepository).save(any(Note.class));
        }

        @Test
        @DisplayName("should use AI_GENERATED source when specified")
        void createNote_withAiSource() {
            when(noteRepository.save(any(Note.class))).thenAnswer(invocation -> {
                Note n = invocation.getArgument(0);
                n.setId(2L);
                return n;
            });

            NoteDTO input = new NoteDTO();
            input.setTitle("AI Note");
            input.setContent("AI content");
            input.setCourseId(10L);
            input.setUserId(1L);
            input.setSource("AI_GENERATED");

            NoteDTO result = noteService.createNote(input);

            assertThat(result.getSource()).isEqualTo("AI_GENERATED");
        }
    }

    @Nested
    @DisplayName("updateNote")
    class UpdateNote {

        @Test
        @DisplayName("should update existing note fields")
        void updateNote_success() {
            when(noteRepository.findById(1L)).thenReturn(Optional.of(sampleNote));
            when(noteRepository.save(any(Note.class))).thenAnswer(invocation -> invocation.getArgument(0));

            NoteDTO update = new NoteDTO();
            update.setTitle("Updated Title");
            update.setContent("Updated content");

            NoteDTO result = noteService.updateNote(1L, update);

            assertThat(result.getTitle()).isEqualTo("Updated Title");
            assertThat(result.getContent()).isEqualTo("Updated content");
            // Tags should remain unchanged
            assertThat(result.getTags()).isEqualTo("java,spring");

            verify(noteRepository).save(any(Note.class));
        }

        @Test
        @DisplayName("should throw when note not found")
        void updateNote_notFound() {
            when(noteRepository.findById(999L)).thenReturn(Optional.empty());

            NoteDTO update = new NoteDTO();
            update.setTitle("New Title");

            assertThatThrownBy(() -> noteService.updateNote(999L, update))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("笔记不存在");

            verify(noteRepository, never()).save(any());
        }

        @Test
        @DisplayName("should not overwrite fields with null values")
        void updateNote_partialUpdate() {
            when(noteRepository.findById(1L)).thenReturn(Optional.of(sampleNote));
            when(noteRepository.save(any(Note.class))).thenAnswer(invocation -> invocation.getArgument(0));

            NoteDTO update = new NoteDTO();
            update.setTitle("New Title Only");
            // all other fields null

            NoteDTO result = noteService.updateNote(1L, update);

            assertThat(result.getTitle()).isEqualTo("New Title Only");
            assertThat(result.getContent()).isEqualTo("Note content");
            assertThat(result.getTags()).isEqualTo("java,spring");
        }

        @Test
        @DisplayName("should update status when provided")
        void updateNote_updateStatus() {
            when(noteRepository.findById(1L)).thenReturn(Optional.of(sampleNote));
            when(noteRepository.save(any(Note.class))).thenAnswer(invocation -> invocation.getArgument(0));

            NoteDTO update = new NoteDTO();
            update.setStatus("PUBLISHED");

            NoteDTO result = noteService.updateNote(1L, update);

            assertThat(result.getStatus()).isEqualTo("PUBLISHED");
        }
    }

    @Nested
    @DisplayName("deleteNote")
    class DeleteNote {

        @Test
        @DisplayName("should delete note when it exists")
        void deleteNote_success() {
            when(noteRepository.existsById(1L)).thenReturn(true);
            doNothing().when(noteRepository).deleteById(1L);

            noteService.deleteNote(1L);

            verify(noteRepository).deleteById(1L);
        }

        @Test
        @DisplayName("should throw when note does not exist")
        void deleteNote_notFound() {
            when(noteRepository.existsById(999L)).thenReturn(false);

            assertThatThrownBy(() -> noteService.deleteNote(999L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("笔记不存在");

            verify(noteRepository, never()).deleteById(anyLong());
        }
    }

    @Nested
    @DisplayName("getNoteById")
    class GetNoteById {

        @Test
        @DisplayName("should return note DTO when found")
        void getNoteById_success() {
            when(noteRepository.findById(1L)).thenReturn(Optional.of(sampleNote));

            NoteDTO result = noteService.getNoteById(1L);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getTitle()).isEqualTo("My Note");
            assertThat(result.getStatus()).isEqualTo("DRAFT");
            assertThat(result.getShared()).isFalse();
        }

        @Test
        @DisplayName("should throw when not found")
        void getNoteById_notFound() {
            when(noteRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> noteService.getNoteById(999L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("笔记不存在");
        }
    }

    @Nested
    @DisplayName("getNotesByUser")
    class GetNotesByUser {

        @Test
        @DisplayName("should return notes for a given user")
        void getNotesByUser_success() {
            Note note2 = new Note();
            note2.setId(2L);
            note2.setTitle("Second Note");
            note2.setUserId(1L);
            note2.setSource(Note.NoteSource.MANUAL);
            note2.setStatus(Note.NoteStatus.DRAFT);

            when(noteRepository.findByUserIdOrderByUpdatedAtDesc(1L))
                    .thenReturn(List.of(sampleNote, note2));

            List<NoteDTO> result = noteService.getNotesByUser(1L);

            assertThat(result).hasSize(2);
            assertThat(result.get(0).getTitle()).isEqualTo("My Note");
            assertThat(result.get(1).getTitle()).isEqualTo("Second Note");
        }

        @Test
        @DisplayName("should return empty list when user has no notes")
        void getNotesByUser_empty() {
            when(noteRepository.findByUserIdOrderByUpdatedAtDesc(999L))
                    .thenReturn(List.of());

            List<NoteDTO> result = noteService.getNotesByUser(999L);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("getNotesByCourseAndUser")
    class GetNotesByCourseAndUser {

        @Test
        @DisplayName("should return notes for a user in a course")
        void getNotesByCourseAndUser_success() {
            when(noteRepository.findByCourseIdAndUserId(10L, 1L))
                    .thenReturn(List.of(sampleNote));

            List<NoteDTO> result = noteService.getNotesByCourseAndUser(10L, 1L);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getCourseId()).isEqualTo(10L);
            assertThat(result.get(0).getUserId()).isEqualTo(1L);
        }
    }

    @Nested
    @DisplayName("searchNotes")
    class SearchNotes {

        @Test
        @DisplayName("should return notes matching keyword")
        void searchNotes_success() {
            when(noteRepository.searchByKeyword(1L, "content"))
                    .thenReturn(List.of(sampleNote));

            List<NoteDTO> result = noteService.searchNotes(1L, "content");

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getContent()).contains("content");
        }

        @Test
        @DisplayName("should return empty list when no matches")
        void searchNotes_noResults() {
            when(noteRepository.searchByKeyword(1L, "nonexistent"))
                    .thenReturn(List.of());

            List<NoteDTO> result = noteService.searchNotes(1L, "nonexistent");

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("shareNote")
    class ShareNote {

        @Test
        @DisplayName("should set shared=true and generate share code")
        void shareNote_success() {
            when(noteRepository.findById(1L)).thenReturn(Optional.of(sampleNote));
            when(noteRepository.save(any(Note.class))).thenAnswer(invocation -> invocation.getArgument(0));

            NoteDTO result = noteService.shareNote(1L);

            assertThat(result.getShared()).isTrue();
            assertThat(result.getShareCode()).isNotNull().isNotEmpty();
            assertThat(sampleNote.getShared()).isTrue();
            assertThat(sampleNote.getShareCode()).isNotNull();
            verify(noteRepository).save(sampleNote);
        }

        @Test
        @DisplayName("should throw when note not found")
        void shareNote_notFound() {
            when(noteRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> noteService.shareNote(999L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("笔记不存在");
        }

        @Test
        @DisplayName("should generate unique share codes on successive calls")
        void shareNote_uniqueCodes() {
            when(noteRepository.findById(1L)).thenReturn(Optional.of(sampleNote));
            when(noteRepository.save(any(Note.class))).thenAnswer(invocation -> invocation.getArgument(0));

            NoteDTO result1 = noteService.shareNote(1L);
            String code1 = result1.getShareCode();

            // Reset note state for second call
            sampleNote.setShareCode(null);
            NoteDTO result2 = noteService.shareNote(1L);
            String code2 = result2.getShareCode();

            // UUIDs should be different (extremely unlikely to collide)
            assertThat(code1).isNotEqualTo(code2);
        }
    }

    @Nested
    @DisplayName("unshareNote")
    class UnshareNote {

        @Test
        @DisplayName("should set shared=false and clear share code")
        void unshareNote_success() {
            sampleNote.setShared(true);
            sampleNote.setShareCode("existing-code");

            when(noteRepository.findById(1L)).thenReturn(Optional.of(sampleNote));
            when(noteRepository.save(any(Note.class))).thenAnswer(invocation -> invocation.getArgument(0));

            NoteDTO result = noteService.unshareNote(1L);

            assertThat(result.getShared()).isFalse();
            assertThat(result.getShareCode()).isNull();
            assertThat(sampleNote.getShared()).isFalse();
            assertThat(sampleNote.getShareCode()).isNull();
            verify(noteRepository).save(sampleNote);
        }

        @Test
        @DisplayName("should throw when note not found")
        void unshareNote_notFound() {
            when(noteRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> noteService.unshareNote(999L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("笔记不存在");
        }
    }

    @Nested
    @DisplayName("getNoteByShareCode")
    class GetNoteByShareCode {

        @Test
        @DisplayName("should return note by share code")
        void getNoteByShareCode_success() {
            sampleNote.setShared(true);
            sampleNote.setShareCode("abc123");

            when(noteRepository.findByShareCode("abc123")).thenReturn(Optional.of(sampleNote));

            NoteDTO result = noteService.getNoteByShareCode("abc123");

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getShareCode()).isEqualTo("abc123");
        }

        @Test
        @DisplayName("should throw when share code is invalid")
        void getNoteByShareCode_invalid() {
            when(noteRepository.findByShareCode("invalid")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> noteService.getNoteByShareCode("invalid"))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("分享链接无效或已过期");
        }
    }

    @Nested
    @DisplayName("getSharedNotes")
    class GetSharedNotes {

        @Test
        @DisplayName("should return all shared notes")
        void getSharedNotes_success() {
            Note sharedNote = new Note();
            sharedNote.setId(2L);
            sharedNote.setTitle("Shared Note");
            sharedNote.setShared(true);
            sharedNote.setShareCode("code123");
            sharedNote.setSource(Note.NoteSource.MANUAL);
            sharedNote.setStatus(Note.NoteStatus.DRAFT);
            sharedNote.setUserId(1L);

            when(noteRepository.findBySharedTrueOrderByCreatedAtDesc())
                    .thenReturn(List.of(sharedNote));

            List<NoteDTO> result = noteService.getSharedNotes();

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getTitle()).isEqualTo("Shared Note");
            assertThat(result.get(0).getShared()).isTrue();
        }

        @Test
        @DisplayName("should return empty list when no shared notes")
        void getSharedNotes_empty() {
            when(noteRepository.findBySharedTrueOrderByCreatedAtDesc())
                    .thenReturn(List.of());

            List<NoteDTO> result = noteService.getSharedNotes();

            assertThat(result).isEmpty();
        }
    }
}
