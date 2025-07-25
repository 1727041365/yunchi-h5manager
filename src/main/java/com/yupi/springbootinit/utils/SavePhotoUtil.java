package com.yupi.springbootinit.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.xmlbeans.impl.xb.xsdschema.Public;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Slf4j
public class SavePhotoUtil {
    public static String saveHotPhoto(MultipartFile photo) throws IOException {
// 获取原始文件名
        String originalFilename = photo.getOriginalFilename();
        // 确保原始文件名不为空
        if (originalFilename == null || originalFilename.isEmpty()) {
            // 若为空，生成一个带默认扩展名的文件名
            originalFilename = UUID.randomUUID().toString() + ".png";
        }
        // 获取文件扩展名（包括点号，如.jpg）
        String extension = FilenameUtils.getExtension(originalFilename);
        if (extension == null || extension.isEmpty()) {
            extension = ".png";  // 直接添加带点的扩展名
        } else {
            extension = "." + extension.toLowerCase();  // 统一小写扩展名
        }
        // 生成唯一文件名（使用UUID+正确的扩展名）
        String fileName = UUID.randomUUID().toString() + extension;
        // 确定存储目录
        Path uploadDir = Paths.get(System.getProperty("user.dir"), "uploads", "hot");
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }
        // 保存文件
        Path filePath = uploadDir.resolve(fileName);
        Files.write(filePath, photo.getBytes());
        // 返回相对路径
        return "/uploads/hot/" + fileName;
    }
    public static String saveHInformationPhoto(MultipartFile photo) throws IOException {
        String originalFilename = photo.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            originalFilename = UUID.randomUUID().toString() + ".png";
        }
        // 获取文件扩展名（包括点号，如.jpg）
        String extension = FilenameUtils.getExtension(originalFilename);
        if (extension == null || extension.isEmpty()) {
            extension = ".png";  // 直接添加带点的扩展名
        } else {
            extension = "." + extension.toLowerCase();  // 统一小写扩展名
        }
        // 生成唯一文件名（使用UUID+正确的扩展名）
        String fileName = UUID.randomUUID().toString() + extension;
        // 确定存储目录
        Path uploadDir = Paths.get(System.getProperty("user.dir"), "uploads", "information");
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }
        // 保存文件
        Path filePath = uploadDir.resolve(fileName);
        Files.write(filePath, photo.getBytes());
        // 返回相对路径
        return "/uploads/information/" + fileName;
    }
}