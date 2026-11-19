package id.ketemu.model;

import id.ketemu.util.Fmt;

import java.time.LocalDateTime;

public class User {

    private int id;
    private String nrp;
    private String fullName;
    private String email;
    private String passwordHash;
    private String phone;
    private String role;
    private LocalDateTime createdAt;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNrp() {
        return nrp;
    }

    public void setNrp(String nrp) {
        this.nrp = nrp;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isAdmin() {
        return "admin".equals(role);
    }

    public String getFirstName() {
        if (fullName == null) {
            return "";
        }
        int sp = fullName.indexOf(' ');
        return sp > 0 ? fullName.substring(0, sp) : fullName;
    }

    public String getJoined() {
        return Fmt.date(createdAt == null ? null : createdAt.toLocalDate());
    }
}
