package com.portfolio.backend.service;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.FileContent;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.drive.model.Permission;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.UserCredentials;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class GoogleDriveService {

    @Value("${google.drive.client.id}")
    private String clientId;

    @Value("${google.drive.client.secret}")
    private String clientSecret;

    @Value("${google.drive.refresh.token}")
    private String refreshToken;

    @Value("${google.drive.root.folder.id:}")
    private String rootFolderId;

    private static final Logger logger = LoggerFactory.getLogger(GoogleDriveService.class);

    private Drive driveService;

    @PostConstruct
    public void init() {
        try {
            logger.info("Initializing Google Drive Service with OAuth 2.0 User Credentials");

            UserCredentials credentials = UserCredentials.newBuilder()
                    .setClientId(clientId)
                    .setClientSecret(clientSecret)
                    .setRefreshToken(refreshToken)
                    .build();

            driveService = new Drive.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    GsonFactory.getDefaultInstance(),
                    new HttpCredentialsAdapter(credentials))
                    .setApplicationName("Portfolio26")
                    .build();
            logger.info("Google Drive Service initialized successfully via OAuth 2.0");
        } catch (Exception e) {
            logger.error("Failed to initialize Google Drive Service via OAuth 2.0. Error: {}", e.getMessage(), e);
        }
    }

    public String getOrCreateFolder(String folderName, String parentId) throws IOException {
        if (driveService == null) {
            throw new IOException("Google Drive Service is not initialized. Check server logs for startup errors.");
        }
        String query = String.format(
                "name = '%s' and mimeType = 'application/vnd.google-apps.folder' and trashed = false", folderName);
        if (parentId != null) {
            query += String.format(" and '%s' in parents", parentId);
        }

        FileList result = driveService.files().list()
                .setQ(query)
                .setSpaces("drive")
                .setFields("files(id, name)")
                .execute();

        List<File> files = result.getFiles();
        if (files == null || files.isEmpty()) {
            File fileMetadata = new File();
            fileMetadata.setName(folderName);
            fileMetadata.setMimeType("application/vnd.google-apps.folder");
            if (parentId != null) {
                fileMetadata.setParents(Collections.singletonList(parentId));
            }

            File file = driveService.files().create(fileMetadata)
                    .setFields("id")
                    .execute();
            return file.getId();
        } else {
            return files.get(0).getId();
        }
    }

    public String uploadFile(MultipartFile multipartFile) throws IOException {
        if (driveService == null) {
            throw new IOException(
                    "Google Drive Service is not initialized. Please verify your credentials and restart the server.");
        }

        try {
            // Create folder structure: (root) -> portfolio26 -> images
            // If rootFolderId is empty, it will use the service account's "My Drive" (which
            // has no quota)
            // If rootFolderId is provided, it will create/find folders inside that shared
            // folder
            String actualRootId = (rootFolderId != null && !rootFolderId.trim().isEmpty()) ? rootFolderId : null;

            String portfolioFolderId = getOrCreateFolder("portfolio26", actualRootId);
            String imagesFolderId = getOrCreateFolder("images", portfolioFolderId);

            String fileName = System.currentTimeMillis() + "_" + multipartFile.getOriginalFilename();
            logger.debug("Uploading file: {} to folder id: {}", fileName, imagesFolderId);

            File fileMetadata = new File();
            fileMetadata.setName(fileName);
            fileMetadata.setParents(Collections.singletonList(imagesFolderId));

            java.io.File tempFile = java.io.File.createTempFile("upload-", fileName);
            multipartFile.transferTo(tempFile);

            FileContent mediaContent = new FileContent(multipartFile.getContentType(), tempFile);
            File file = driveService.files().create(fileMetadata, mediaContent)
                    .setFields("id, webContentLink, webViewLink")
                    .execute();

            tempFile.delete();

            // 1. Set file permissions to 'anyone with the link can view'
            logger.debug("Setting public permissions for file id: {}", file.getId());
            Permission permission = new Permission();
            permission.setType("anyone");
            permission.setRole("reader");
            driveService.permissions().create(file.getId(), permission).execute();

            // 2. Return a direct preview/embed link instead of the download link
            // Use drive.google.com/thumbnail for better stability and rate-limit tolerance
            return String.format("https://drive.google.com/thumbnail?id=%s&sz=w1200", file.getId());
        } catch (Exception e) {
            logger.error("Failed to upload file to Google Drive: {}", e.getMessage(), e);
            throw new IOException("Error during Google Drive upload: " + e.getMessage(), e);
        }
    }

    public byte[] downloadFile(String fileId) throws IOException {
        if (driveService == null) {
            throw new IOException("Google Drive Service is not initialized.");
        }

        try (java.io.ByteArrayOutputStream outputStream = new java.io.ByteArrayOutputStream()) {
            driveService.files().get(fileId).executeMediaAndDownloadTo(outputStream);
            return outputStream.toByteArray();
        }
    }
}
