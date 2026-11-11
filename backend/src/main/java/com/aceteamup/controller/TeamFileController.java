package com.aceteamup.controller;

import com.aceteamup.dto.ApiResponse;
import com.aceteamup.dto.workspace.TeamFileResponse;
import com.aceteamup.service.FileStorageService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/teams/{teamId}/files")
public class TeamFileController {

    private final FileStorageService fileStorageService;

    public TeamFileController(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<TeamFileResponse>> uploadFile(
            @PathVariable Long teamId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "category", required = false) String category,
            @AuthenticationPrincipal UserDetails userDetails) {
        TeamFileResponse response = fileStorageService.uploadFile(userDetails.getUsername(), teamId, file, category);
        return ResponseEntity.ok(ApiResponse.success("File uploaded successfully", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<TeamFileResponse>>> getFiles(
            @PathVariable Long teamId,
            @RequestParam(value = "category", required = false) String category,
            @AuthenticationPrincipal UserDetails userDetails) {
        List<TeamFileResponse> files = fileStorageService.getTeamFiles(userDetails.getUsername(), teamId, category);
        return ResponseEntity.ok(ApiResponse.success(files));
    }

    @GetMapping("/{fileId}/download")
    public ResponseEntity<Resource> downloadFile(
            @PathVariable Long teamId,
            @PathVariable Long fileId,
            @AuthenticationPrincipal UserDetails userDetails) {
        Resource resource = fileStorageService.loadFileAsResource(userDetails.getUsername(), teamId, fileId);

        String contentType = "application/octet-stream";
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    @DeleteMapping("/{fileId}")
    public ResponseEntity<ApiResponse<Void>> deleteFile(
            @PathVariable Long teamId,
            @PathVariable Long fileId,
            @AuthenticationPrincipal UserDetails userDetails) {
        fileStorageService.deleteFile(userDetails.getUsername(), teamId, fileId);
        return ResponseEntity.ok(ApiResponse.success("File deleted successfully", null));
    }
}

