package id.ketemu.model;

import id.ketemu.util.Fmt;
import id.ketemu.util.Icons;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Item {

    private int id;
    private String code;
    private int reporterId;
    private String reporterName;
    private LocalDateTime reporterSince;
    private int categoryId;
    private String categoryName;
    private String categoryIcon;
    private int locationId;
    private String locationName;
    private String kind;
    private String title;
    private String description;
    private String locationDetail;
    private LocalDate eventDate;
    private Integer heldAtId;
    private String heldAtName;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer coverPhotoId;
    private int photoCount;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public int getReporterId() {
        return reporterId;
    }

    public void setReporterId(int reporterId) {
        this.reporterId = reporterId;
    }

    public String getReporterName() {
        return reporterName;
    }

    public void setReporterName(String reporterName) {
        this.reporterName = reporterName;
    }

    public LocalDateTime getReporterSince() {
        return reporterSince;
    }

    public void setReporterSince(LocalDateTime reporterSince) {
        this.reporterSince = reporterSince;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getCategoryIcon() {
        return categoryIcon;
    }

    public void setCategoryIcon(String categoryIcon) {
        this.categoryIcon = categoryIcon;
    }

    public int getLocationId() {
        return locationId;
    }

    public void setLocationId(int locationId) {
        this.locationId = locationId;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLocationDetail() {
        return locationDetail;
    }

    public void setLocationDetail(String locationDetail) {
        this.locationDetail = locationDetail;
    }

    public LocalDate getEventDate() {
        return eventDate;
    }

    public void setEventDate(LocalDate eventDate) {
        this.eventDate = eventDate;
    }

    public Integer getHeldAtId() {
        return heldAtId;
    }

    public void setHeldAtId(Integer heldAtId) {
        this.heldAtId = heldAtId;
    }

    public String getHeldAtName() {
        return heldAtName;
    }

    public void setHeldAtName(String heldAtName) {
        this.heldAtName = heldAtName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Integer getCoverPhotoId() {
        return coverPhotoId;
    }

    public void setCoverPhotoId(Integer coverPhotoId) {
        this.coverPhotoId = coverPhotoId;
    }

    public int getPhotoCount() {
        return photoCount;
    }

    public void setPhotoCount(int photoCount) {
        this.photoCount = photoCount;
    }

    /** The word on the rubber stamp: Lost or Found while open, then the item's status. */
    public String getStamp() {
        if ("open".equals(status)) {
            return kind;
        }
        return status;
    }

    public boolean isLost() {
        return "lost".equals(kind);
    }

    public boolean isFound() {
        return "found".equals(kind);
    }

    public boolean isOpen() {
        return "open".equals(status);
    }

    /** Small, stable tilt between -2.5 and 2.5 degrees, derived from the id. */
    public String getTilt() {
        int step = Math.floorMod(id * 37 + 11, 11) - 5;
        return String.valueOf(step / 2.0);
    }

    public String getStampTilt() {
        int step = Math.floorMod(id * 53 + 7, 9) - 4;
        return String.valueOf(step * 2 + (isLost() ? -3 : 4));
    }

    /** Place shown on the tag: location plus the detail, if any. */
    public String getPlace() {
        if (locationDetail == null || locationDetail.isBlank()) {
            return locationName;
        }
        return locationName + ", " + locationDetail;
    }

    /** Icon for the photo well, guessed from the title, falling back to the category icon. */
    public String getIcon() {
        return Icons.guess(title, categoryIcon);
    }

    public String getDateShort() {
        return Fmt.dayMonth(eventDate);
    }

    public String getDateLong() {
        return Fmt.longDate(eventDate);
    }

    public String getDateIso() {
        return eventDate == null ? "" : eventDate.toString();
    }

    public String getPosted() {
        return Fmt.ago(createdAt);
    }

    public String getReporterFirstName() {
        if (reporterName == null) {
            return "";
        }
        int sp = reporterName.indexOf(' ');
        return sp > 0 ? reporterName.substring(0, sp) : reporterName;
    }
}
