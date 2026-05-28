package com.autodev.note.controller;

import com.autodev.note.common.Result;
import com.autodev.note.dto.FileRecordDTO;
import com.autodev.note.entity.FileRecord;
import com.autodev.note.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/file")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    /**
     * 上传文件
     * POST /api/file/upload
     */
    @PostMapping("/upload")
    public Result<FileRecordDTO> upload(@RequestParam("file") MultipartFile file,
                                         @RequestParam("userId") Long userId,
                                         @RequestParam(value = "courseId", required = false) Long courseId,
                                         @RequestParam(value = "fileType", defaultValue = "DOCUMENT") String fileType) {
        FileRecord.FileType type;
        try {
            type = FileRecord.FileType.valueOf(fileType.toUpperCase());
        } catch (IllegalArgumentException e) {
            type = FileRecord.FileType.DOCUMENT;
        }
        return Result.success(fileService.upload(file, userId, courseId, type));
    }

    /**
     * 下载文件
     * GET /api/file/download/{id}
     */
    @GetMapping("/download/{id}")
    public ResponseEntity<InputStreamResource> download(@PathVariable Long id) {
        FileRecordDTO record = fileService.getFileRecord(id);
        InputStream is = fileService.download(id);

        String encodedName = URLEncoder.encode(record.getOriginalName(), StandardCharsets.UTF_8)
                .replace("+", "%20");

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encodedName)
                .contentType(MediaType.parseMediaType(record.getContentType()))
                .body(new InputStreamResource(is));
    }

    /**
     * 获取文件信息
     * GET /api/file/{id}
     */
    @GetMapping("/{id}")
    public Result<FileRecordDTO> getFileRecord(@PathVariable Long id) {
        return Result.success(fileService.getFileRecord(id));
    }

    /**
     * 删除文件
     * DELETE /api/file/{id}
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteFile(@PathVariable Long id) {
        fileService.delete(id);
        return Result.success();
    }
}
