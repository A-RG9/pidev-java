package com.wellcare.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class VideoUploadService {

    private static final String VIDEO_UPLOAD_DIR = "uploads/videos/";

    public static String saveLocalVideo(File videoFile, String exerciseName) throws IOException {
        Path uploadPath = Paths.get(VIDEO_UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String safeName = exerciseName.replaceAll("[^a-zA-Z0-9]", "_");
        String fileName = timestamp + "_" + safeName + getFileExtension(videoFile);
        Path destination = uploadPath.resolve(fileName);

        Files.copy(videoFile.toPath(), destination, StandardCopyOption.REPLACE_EXISTING);

        return destination.toString();
    }

    public static boolean deleteLocalVideo(String videoPath) {
        if (videoPath != null && !videoPath.isEmpty() && videoPath.startsWith("uploads/")) {
            try {
                Files.deleteIfExists(Paths.get(videoPath));
                return true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public static boolean isValidYouTubeUrl(String url) {
        if (url == null || url.isEmpty()) return false;
        return url.contains("youtube.com/watch?v=") || url.contains("youtu.be/");
    }

    public static String convertToEmbedUrl(String youtubeUrl) {
        try {
            if (youtubeUrl.contains("youtube.com/watch?v=")) {
                String videoId = youtubeUrl.substring(youtubeUrl.indexOf("v=") + 2);
                int ampIndex = videoId.indexOf('&');
                if (ampIndex != -1) {
                    videoId = videoId.substring(0, ampIndex);
                }
                return "https://www.youtube.com/embed/" + videoId + "?autoplay=0&modestbranding=1&rel=0";
            } else if (youtubeUrl.contains("youtu.be/")) {
                String videoId = youtubeUrl.substring(youtubeUrl.lastIndexOf("/") + 1);
                return "https://www.youtube.com/embed/" + videoId;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return youtubeUrl;
    }

    private static String getFileExtension(File file) {
        String name = file.getName();
        int lastDot = name.lastIndexOf('.');
        return lastDot > 0 ? name.substring(lastDot) : "";
    }
}