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
    private static final String GIF_UPLOAD_DIR = "uploads/gifs/";

    public static String saveLocalVideo(File videoFile, String exerciseName) throws IOException {
        // Créer le dossier s'il n'existe pas
        Path uploadPath = Paths.get(VIDEO_UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
            System.out.println("📁 Dossier créé: " + uploadPath.toAbsolutePath());
        }

        // Générer un nom de fichier unique
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String safeName = exerciseName.replaceAll("[^a-zA-Z0-9]", "_");
        if (safeName.length() > 30) {
            safeName = safeName.substring(0, 30);
        }
        String extension = getFileExtension(videoFile);
        String fileName = timestamp + "_" + safeName + extension;

        Path destination = uploadPath.resolve(fileName);
        Files.copy(videoFile.toPath(), destination, StandardCopyOption.REPLACE_EXISTING);

        String relativePath = VIDEO_UPLOAD_DIR + fileName;
        System.out.println("✅ Vidéo sauvegardée: " + relativePath);
        System.out.println("📍 Chemin absolu: " + destination.toAbsolutePath());

        return relativePath;
    }

    public static String saveLocalGif(File gifFile, String exerciseName) throws IOException {
        Path uploadPath = Paths.get(GIF_UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String safeName = exerciseName.replaceAll("[^a-zA-Z0-9]", "_");
        String fileName = timestamp + "_" + safeName + ".gif";

        Path destination = uploadPath.resolve(fileName);
        Files.copy(gifFile.toPath(), destination, StandardCopyOption.REPLACE_EXISTING);

        return GIF_UPLOAD_DIR + fileName;
    }

    public static boolean deleteLocalVideo(String videoPath) {
        if (videoPath != null && !videoPath.isEmpty()) {
            try {
                Path path = Paths.get(videoPath);
                if (Files.exists(path)) {
                    Files.deleteIfExists(path);
                    System.out.println("🗑️ Vidéo supprimée: " + videoPath);
                    return true;
                }
            } catch (IOException e) {
                System.err.println("❌ Erreur suppression vidéo: " + e.getMessage());
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
        return lastDot > 0 ? name.substring(lastDot) : ".mp4";
    }

    // Vérifier si un fichier existe
    public static boolean videoFileExists(String videoPath) {
        if (videoPath == null || videoPath.isEmpty()) return false;
        File file = new File(videoPath);
        if (file.exists()) return true;

        // Vérifier depuis le dossier uploads/videos/
        File uploadsFile = new File("uploads/videos/" + new File(videoPath).getName());
        return uploadsFile.exists();
    }
}