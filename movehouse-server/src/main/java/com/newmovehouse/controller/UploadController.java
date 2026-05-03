package com.newmovehouse.controller;

import com.newmovehouse.common.ApiResponse;
import com.newmovehouse.common.BizException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 通用文件上传：保存至配置目录（默认项目下 uploads），返回可访问的 {@code /files/...} URL。
 */
@RestController
@RequestMapping("/api/upload")
public class UploadController {

    @Value("${upload.base-dir:uploads}")
    private String uploadBaseDir;

    /**
     * 上传单个文件。
     *
     * @return fileUrl、originalName
     */
    @PostMapping
    public ApiResponse<Map<String, String>> upload(@RequestParam("file") MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new BizException("文件为空");
        }
        String original = file.getOriginalFilename() == null ? "file" : file.getOriginalFilename();
        String ext = "";
        int idx = original.lastIndexOf('.');
        if (idx >= 0) {
            ext = original.substring(idx);
        }
        String name = UUID.randomUUID().toString().replace("-", "") + ext;
        Path dir = Paths.get(uploadBaseDir);
        Files.createDirectories(dir);
        Path target = dir.resolve(name);
        file.transferTo(target.toFile());

        Map<String, String> res = new HashMap<>();
        res.put("fileUrl", "/files/" + name);
        res.put("originalName", original);
        return ApiResponse.ok(res);
    }
}
