package org.example.utils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class JitsiService {

    private static final String JITSI_SERVER = "https://meet.jit.si/";
    private static final String APP_NAME = "Wellora";

    public String createMeetingRoom(String roomName) {
        return JITSI_SERVER + APP_NAME + "_" + sanitizeRoomName(roomName);
    }

    public String createPrivateMeeting(String coachId, String patientId) {
        String coachShort = coachId.length() > 8 ? coachId.substring(0, 8) : coachId;
        String patientShort = patientId.length() > 8 ? patientId.substring(0, 8) : patientId;
        String timestamp = String.valueOf(System.currentTimeMillis()).substring(8);
        String roomName = coachShort + "_" + patientShort + "_" + timestamp;
        return JITSI_SERVER + APP_NAME + "_" + roomName;
    }

    public String getConfiguredUrl(String roomName, String userName) {
        String baseUrl = roomName.startsWith("http") ? roomName : JITSI_SERVER + sanitizeRoomName(roomName);
        return baseUrl +
                "#config.prejoinPageEnabled=false" +
                "&userInfo.displayName=" + encode(userName);
    }

    private String sanitizeRoomName(String name) {
        if (name == null) return "default_room";
        return name.replaceAll("[^a-zA-Z0-9_-]", "_");
    }

    private String encode(String text) {
        try {
            return URLEncoder.encode(text, StandardCharsets.UTF_8.toString());
        } catch (Exception e) {
            return text.replace(" ", "%20");
        }
    }
}