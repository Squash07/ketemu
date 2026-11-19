package id.ketemu.model;

import id.ketemu.util.Fmt;

import java.time.LocalDateTime;

public class Claim {

    private int id;
    private int itemId;
    private String itemCode;
    private String itemTitle;
    private String itemKind;
    private String itemStatus;
    private String itemIcon;
    private int finderId;
    private String finderName;
    private String finderEmail;
    private String finderPhone;
    private Integer heldAtId;
    private String heldAtName;
    private int claimantId;
    private String claimantName;
    private String claimantEmail;
    private String claimantPhone;
    private String proof;
    private Integer meetLocationId;
    private String meetLocationName;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime decidedAt;
    private int seq;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getItemId() {
        return itemId;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    public String getItemCode() {
        return itemCode;
    }

    public void setItemCode(String itemCode) {
        this.itemCode = itemCode;
    }

    public String getItemTitle() {
        return itemTitle;
    }

    public void setItemTitle(String itemTitle) {
        this.itemTitle = itemTitle;
    }

    public String getItemKind() {
        return itemKind;
    }

    public void setItemKind(String itemKind) {
        this.itemKind = itemKind;
    }

    public String getItemStatus() {
        return itemStatus;
    }

    public void setItemStatus(String itemStatus) {
        this.itemStatus = itemStatus;
    }

    public String getItemIcon() {
        return itemIcon;
    }

    public void setItemIcon(String itemIcon) {
        this.itemIcon = itemIcon;
    }

    public int getFinderId() {
        return finderId;
    }

    public void setFinderId(int finderId) {
        this.finderId = finderId;
    }

    public String getFinderName() {
        return finderName;
    }

    public void setFinderName(String finderName) {
        this.finderName = finderName;
    }

    public String getFinderEmail() {
        return finderEmail;
    }

    public void setFinderEmail(String finderEmail) {
        this.finderEmail = finderEmail;
    }

    public String getFinderPhone() {
        return finderPhone;
    }

    public void setFinderPhone(String finderPhone) {
        this.finderPhone = finderPhone;
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

    public int getClaimantId() {
        return claimantId;
    }

    public void setClaimantId(int claimantId) {
        this.claimantId = claimantId;
    }

    public String getClaimantName() {
        return claimantName;
    }

    public void setClaimantName(String claimantName) {
        this.claimantName = claimantName;
    }

    public String getClaimantEmail() {
        return claimantEmail;
    }

    public void setClaimantEmail(String claimantEmail) {
        this.claimantEmail = claimantEmail;
    }

    public String getClaimantPhone() {
        return claimantPhone;
    }

    public void setClaimantPhone(String claimantPhone) {
        this.claimantPhone = claimantPhone;
    }

    public String getProof() {
        return proof;
    }

    public void setProof(String proof) {
        this.proof = proof;
    }

    public Integer getMeetLocationId() {
        return meetLocationId;
    }

    public void setMeetLocationId(Integer meetLocationId) {
        this.meetLocationId = meetLocationId;
    }

    public String getMeetLocationName() {
        return meetLocationName;
    }

    public void setMeetLocationName(String meetLocationName) {
        this.meetLocationName = meetLocationName;
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

    public LocalDateTime getDecidedAt() {
        return decidedAt;
    }

    public void setDecidedAt(LocalDateTime decidedAt) {
        this.decidedAt = decidedAt;
    }

    public int getSeq() {
        return seq;
    }

    public void setSeq(int seq) {
        this.seq = seq;
    }

    /** Claim number printed on the ticket, for example K-0418-03. */
    public String getNumber() {
        return itemCode + "-" + getStub();
    }

    public String getStub() {
        return String.format("%02d", seq);
    }

    public boolean isPending() {
        return "pending".equals(status);
    }

    public boolean isApproved() {
        return "approved".equals(status);
    }

    public boolean isActive() {
        return isPending() || isApproved();
    }

    /** Plain-language status line for the ticket. */
    public String getStatusLine() {
        switch (status) {
            case "pending":
                return "Waiting for the finder's answer";
            case "approved":
                return "returned".equals(itemStatus) ? "Handed over. This item is back with its owner." : "Approved. Meet the finder at the pickup post.";
            case "rejected":
                return "The finder did not approve this claim";
            default:
                return "You cancelled this claim";
        }
    }

    public String getFiled() {
        return Fmt.ago(createdAt);
    }

    public String getFiledDate() {
        return Fmt.date(createdAt == null ? null : createdAt.toLocalDate());
    }

    public String getDecided() {
        return Fmt.ago(decidedAt);
    }
}
