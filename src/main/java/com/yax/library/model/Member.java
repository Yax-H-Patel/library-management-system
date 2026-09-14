package com.yax.library.model;

import java.time.LocalDate;

public class Member {
    private int memberId;
    private String name, email, phone;
    private LocalDate joinDate;
    private Status status;
    enum Status {
        ACTIVE,
        SUSPENDED
    }

    public Member(String name, String email, LocalDate joinDate, String phone) {
        this.name = name;
        this.email = email;
        this.joinDate = joinDate;
        this.phone = phone;
        this.status = Status.ACTIVE;
    }

    public Member(int memberId, String name, String email, LocalDate joinDate, String phone, Status status) {
        this.memberId = memberId;
        this.name = name;
        this.email = email;
        this.joinDate = joinDate;
        this.phone = phone;
        this.status = status;
    }

    public int getMemberId() {
        return memberId;
    }

    public void setMemberId(int memberId) {
        this.memberId = memberId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public LocalDate getJoinDate() {
        return joinDate;
    }

    public void setJoinDate(LocalDate joinDate) {
        this.joinDate = joinDate;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return String.format("[%d] %s | %s | %s | Joined: %s | Status: %s",
                memberId, name, email, phone, joinDate, status);
    }

}
