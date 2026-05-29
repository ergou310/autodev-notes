package com.autodev.note.controller;

import com.autodev.note.dto.NoteDTO;
import com.autodev.note.exception.GlobalExceptionHandler;
import com.autodev.note.service.NoteService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.bean.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NoteController.class)
@Import(GlobalExceptionHandler.class)
@TestPropertySource(properties = {
        "spring.cloud.nacos.discovery.enabled=false",
        "spring.cloud.config.enabled=false"
})
class NoteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NoteService noteService;

    @Autowired
    private ObjectMapper objectMapper;

    private NoteDTO sampleNoteDTO;

    @BeforeEach
    void setUp() {
        sampleNoteDTO = new NoteDTO();
        sampleNoteDTO.setId(1L);
        sampleNoteDTO.setTitle("My Note");
        sampleNoteDTO.setContent("Note content in markdown");
        sampleNoteDTO.setMindMap("{\"nodes\":[]}");
        sampleNoteDTO.setQuizQuestions("{\"questions\":[]}");
        sampleNoteDTO.setCourseId(10L);
        sampleNoteDTO.setUserId(1L);
        sampleNoteDTO.setSource("MANUAL");
        sampleNoteDTO.setSourceFileId(null);
        sampleNoteDTO.setShared(false);
        sampleNoteDTO.setShareCode(null);
        sampleNoteDTO.setTags("java,spring");
        sampleNoteDTO.setStatus("DRAFT");
        sampleNoteDTO.setCreatedAt(LocalDateTime.now());
        sampleNoteDTO.setUpdatedAt(LocalDateTime.now());
    }

    @Nested
    @DisplayName("POST /api/note")
    class CreateNote {

        @Test
        @DisplayName("should create note and return 200 with note data")
        void createNote_success() throws Exception {
            when(noteService.createNote(any(NoteDTO.class))).thenReturn(sampleNoteDTO);

            mockMvc.perform(post("/api/note")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleNoteDTO)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code", is(200)))
                    .andExpect(jsonPath("$.message", is("success")))
                    .andExpect(jsonPath("$.data.id", is(1)))
                    .andExpect(jsonPath("$.data.title", is("My Note")))
                    .andExpect(jsonPath("$.data.content", is("Note content in markdown")))
                    .andExpect(jsonPath("$.data.userId", is(1)))
                    .andExpect(jsonPath("$.data.tags", is("java,spring")));

            verify(noteService).createNote(any(NoteDTO.class));
        }
    }

    @Nested
    @DisplayName("PUT /api/note/{id}")
    class UpdateNote {

        @Test
        @DisplayName("should update note and return updated data")
        void updateNote_success() throws Exception {
            NoteDTO updated = new NoteDTO();
            updated.setId(1L);
            updated.setTitle("Updated Title");
            updated.setContent("Updated content");
            updated.setCourseId(10L);
            updated.setUserId(1L);
            updated.setStatus("DRAFT");

            when(noteService.updateNote(eq(1L), any(NoteDTO.class))).thenReturn(updated);

            mockMvc.perform(put("/api/note/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updated)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code", is(200)))
                    .andExpect(jsonPath("$.data.title", is("Updated Title")));
        }

        @Test
        @DisplayName("should return 400 when note not found")
        void updateNote_notFound() throws Exception {
            when(noteService.updateNote(eq(999L), any(NoteDTO.class)))
                    .thenThrow(new RuntimeException("笔记不存在"));

            mockMvc.perform(put("/api/note/999")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleNoteDTO)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code", is(400)))
                    .andExpect(jsonPath("$.message", containsString("笔记不存在")));
        }
    }

    @Nested
    @DisplayName("DELETE /api/note/{id}")
    class DeleteNote {

        @Test
        @DisplayName("should delete note and return success")
        void deleteNote_success() throws Exception {
            doNothing().when(noteService).deleteNote(1L);

            mockMvc.perform(delete("/api/note/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code", is(200)));

            verify(noteService).deleteNote(1L);
        }

        @Test
        @DisplayName("should return 400 when note not found")
        void deleteNote_notFound() throws Exception {
            doThrow(new RuntimeException("笔记不存在")).when(noteService).deleteNote(999L);

            mockMvc.perform(delete("/api/note/999"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code", is(400)));
        }
    }

    @Nested
    @DisplayName("GET /api/note/{id}")
    class GetNote {

        @Test
        @DisplayName("should return note by id")
        void getNote_success() throws Exception {
            when(noteService.getNoteById(1L)).thenReturn(sampleNoteDTO);

            mockMvc.perform(get("/api/note/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code", is(200)))
                    .andExpect(jsonPath("$.data.id", is(1)))
                    .andExpect(jsonPath("$.data.title", is("My Note")));
        }

        @Test
        @DisplayName("should return 400 when note not found")
        void getNote_notFound() throws Exception {
            when(noteService.getNoteById(999L))
                    .thenThrow(new RuntimeException("笔记不存在"));

            mockMvc.perform(get("/api/note/999"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code", is(400)));
        }
    }

    @Nested
    @DisplayName("GET /api/note/user/{userId}")
    class GetNotesByUser {

        @Test
        @DisplayName("should return notes for a user")
        void getNotesByUser_success() throws Exception {
            NoteDTO note2 = new NoteDTO();
            note2.setId(2L);
            note2.setTitle("Second Note");
            note2.setUserId(1L);

            when(noteService.getNotesByUser(1L)).thenReturn(List.of(sampleNoteDTO, note2));

            mockMvc.perform(get("/api/note/user/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data", hasSize(2)))
                    .andExpect(jsonPath("$.data[0].title", is("My Note")))
                    .andExpect(jsonPath("$.data[1].title", is("Second Note")));
        }

        @Test
        @DisplayName("should return empty list when user has no notes")
        void getNotesByUser_empty() throws Exception {
            when(noteService.getNotesByUser(999L)).thenReturn(List.of());

            mockMvc.perform(get("/api/note/user/999"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data", hasSize(0)));
        }
    }

    @Nested
    @DisplayName("GET /api/note/user/{userId}/course/{courseId}")
    class GetNotesByCourseAndUser {

        @Test
        @DisplayName("should return notes for a user in a specific course")
        void getNotesByCourseAndUser_success() throws Exception {
            when(noteService.getNotesByCourseAndUser(10L, 1L))
                    .thenReturn(List.of(sampleNoteDTO));

            mockMvc.perform(get("/api/note/user/1/course/10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data", hasSize(1)))
                    .andExpect(jsonPath("$.data[0].courseId", is(10)));
        }
    }

    @Nested
    @DisplayName("GET /api/note/search")
    class SearchNotes {

        @Test
        @DisplayName("should return notes matching keyword")
        void searchNotes_success() throws Exception {
            when(noteService.searchNotes(1L, "spring")).thenReturn(List.of(sampleNoteDTO));

            mockMvc.perform(get("/api/note/search")
                            .param("userId", "1")
                            .param("keyword", "spring"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data", hasSize(1)))
                    .andExpect(jsonPath("$.data[0].title", is("My Note")));
        }

        @Test
        @DisplayName("should return empty list when no matches")
        void searchNotes_noResults() throws Exception {
            when(noteService.searchNotes(1L, "nonexistent")).thenReturn(List.of());

            mockMvc.perform(get("/api/note/search")
                            .param("userId", "1")
                            .param("keyword", "nonexistent"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data", hasSize(0)));
        }
    }

    @Nested
    @DisplayName("PUT /api/note/{id}/share")
    class ShareNote {

        @Test
        @DisplayName("should share note and return share code")
        void shareNote_success() throws Exception {
            sampleNoteDTO.setShared(true);
            sampleNoteDTO.setShareCode("abc123def456");
            when(noteService.shareNote(1L)).thenReturn(sampleNoteDTO);

            mockMvc.perform(put("/api/note/1/share"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.shared", is(true)))
                    .andExpect(jsonPath("$.data.shareCode", is("abc123def456")));
        }
    }

    @Nested
    @DisplayName("PUT /api/note/{id}/unshare")
    class UnshareNote {

        @Test
        @DisplayName("should unshare note")
        void unshareNote_success() throws Exception {
            sampleNoteDTO.setShared(false);
            sampleNoteDTO.setShareCode(null);
            when(noteService.unshareNote(1L)).thenReturn(sampleNoteDTO);

            mockMvc.perform(put("/api/note/1/unshare"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.shared", is(false)));
        }
    }

    @Nested
    @DisplayName("GET /api/note/share/{shareCode}")
    class GetNoteByShareCode {

        @Test
        @DisplayName("should return note by share code")
        void getNoteByShareCode_success() throws Exception {
            sampleNoteDTO.setShared(true);
            sampleNoteDTO.setShareCode("abc123");
            when(noteService.getNoteByShareCode("abc123")).thenReturn(sampleNoteDTO);

            mockMvc.perform(get("/api/note/share/abc123"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.id", is(1)))
                    .andExpect(jsonPath("$.data.shareCode", is("abc123")));
        }

        @Test
        @DisplayName("should return 400 when share code invalid")
        void getNoteByShareCode_invalid() throws Exception {
            when(noteService.getNoteByShareCode("invalid"))
                    .thenThrow(new RuntimeException("分享链接无效或已过期"));

            mockMvc.perform(get("/api/note/share/invalid"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code", is(400)))
                    .andExpect(jsonPath("$.message", containsString("分享链接无效")));
        }
    }

    @Nested
    @DisplayName("GET /api/note/shared")
    class GetSharedNotes {

        @Test
        @DisplayName("should return shared notes list")
        void getSharedNotes_success() throws Exception {
            NoteDTO sharedNote = new NoteDTO();
            sharedNote.setId(2L);
            sharedNote.setTitle("Shared Note");
            sharedNote.setShared(true);
            sharedNote.setShareCode("xyz789");

            when(noteService.getSharedNotes()).thenReturn(List.of(sharedNote));

            mockMvc.perform(get("/api/note/shared"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data", hasSize(1)))
                    .andExpect(jsonPath("$.data[0].title", is("Shared Note")));
        }

        @Test
        @DisplayName("should return empty list when no shared notes")
        void getSharedNotes_empty() throws Exception {
            when(noteService.getSharedNotes()).thenReturn(List.of());

            mockMvc.perform(get("/api/note/shared"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data", hasSize(0)));
        }
    }
}
