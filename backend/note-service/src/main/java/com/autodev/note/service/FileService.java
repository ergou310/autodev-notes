package com.autodev.note.service;

import com.autodev.note.config.MinioConfig;
import com.autodev.note.dto.FileRecordDTO;
import com.autodev.note.entity.FileRecord;
import com.autodev.note.repository.FileRecordRepository;
import io.minio.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileService {

    private final MinioClient minioClient;
    private final MinioConfig minioConfig;
    private final FileRecordRepository fileRecordRepo;

    /**
     * 上传文件到 MinIO 并保存记录
     */
    @Transactional
    public FileRecordDTO upload(MultipartFile file, Long userId, Long courseId, FileRecord.FileType fileType) {
        try {
            // 确保 bucket 存在
            boolean exists = minioClient.bucketExists(
                    BucketExistsArgs.builder().bucket(minioConfig.getBucket()).build());
            if (!exists) {
                minioClient.makeBucket(
                        MakeBucketArgs.builder().bucket(minioConfig.getBucket()).build());
            }

            // 生成存储路径
            String ext = getExtension(file.getOriginalFilename());
            String objectName = "files/" + userId + "/" + UUID.randomUUID() + ext;

            // 上传到 MinIO
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(minioConfig.getBucket())
                    .object(objectName)
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build());

            // 保存文件记录
            FileRecord record = new FileRecord();
            record.setOriginalName(file.getOriginalFilename());
            record.setStoragePath(objectName);
            record.setContentType(file.getContentType());
            record.setFileSize(file.getSize());
            record.setUserId(userId);
            record.setCourseId(courseId);
            record.setFileType(fileType);
            record = fileRecordRepo.save(record);

            FileRecordDTO dto = FileRecordDTO.fromEntity(record);
            dto.setDownloadUrl(minioConfig.getEndpoint() + "/" + minioConfig.getBucket() + "/" + objectName);
            return dto;

        } catch (Exception e) {
            throw new RuntimeException("文件上传失败: " + e.getMessage());
        }
    }

    /**
     * 下载文件
     */
    public InputStream download(Long fileId) {
        FileRecord record = fileRecordRepo.findById(fileId)
                .orElseThrow(() -> new RuntimeException("文件不存在"));
        try {
            return minioClient.getObject(GetObjectArgs.builder()
                    .bucket(minioConfig.getBucket())
                    .object(record.getStoragePath())
                    .build());
        } catch (Exception e) {
            throw new RuntimeException("文件下载失败: " + e.getMessage());
        }
    }

    /**
     * 删除文件
     */
    @Transactional
    public void delete(Long fileId) {
        FileRecord record = fileRecordRepo.findById(fileId)
                .orElseThrow(() -> new RuntimeException("文件不存在"));
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(minioConfig.getBucket())
                    .object(record.getStoragePath())
                    .build());
            fileRecordRepo.deleteById(fileId);
        } catch (Exception e) {
            throw new RuntimeException("文件删除失败: " + e.getMessage());
        }
    }

    public FileRecordDTO getFileRecord(Long fileId) {
        FileRecord record = fileRecordRepo.findById(fileId)
                .orElseThrow(() -> new RuntimeException("文件不存在"));
        FileRecordDTO dto = FileRecordDTO.fromEntity(record);
        dto.setDownloadUrl(minioConfig.getEndpoint() + "/" + minioConfig.getBucket() + "/" + record.getStoragePath());
        return dto;
    }

    private String getExtension(String filename) {
        if (filename == null) return "";
        int idx = filename.lastIndexOf(".");
        return idx >= 0 ? filename.substring(idx) : "";
    }
}
