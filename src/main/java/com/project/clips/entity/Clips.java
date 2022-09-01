package com.project.clips.entity;

import org.bson.types.Binary;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Document(collection = "clips")
public class Clips {
    @Id
    private String id;
    private String title;
    private String displayName;
    private String clipFileName;
    private Binary clipData;
    private String screenshotFileName;
    private Binary screenshotData;
    private String userId;
    private Date timestamp;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getClipFileName() {
        return clipFileName;
    }

    public void setClipFileName(String clipFileName) {
        this.clipFileName = clipFileName;
    }

    public Binary getClipData() {
        return clipData;
    }

    public void setClipData(Binary clipData) {
        this.clipData = clipData;
    }

    public String getScreenshotFileName() {
        return screenshotFileName;
    }

    public void setScreenshotFileName(String screenshotFileName) {
        this.screenshotFileName = screenshotFileName;
    }

    public Binary getScreenshotData() {
        return screenshotData;
    }

    public void setScreenshotData(Binary screenshotData) {
        this.screenshotData = screenshotData;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}
