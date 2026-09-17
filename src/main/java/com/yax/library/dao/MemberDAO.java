package com.yax.library.dao;

import com.yax.library.model.Member;
import com.yax.library.util.DBConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class MemberDAO {

    public boolean addMember(Member member) {
        String sql = "INSERT INTO members (name, email, phone, join_date, status) " + "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, member.getName());
            pstmt.setString(2, member.getEmail());
            pstmt.setString(3, member.getPhone());
            pstmt.setDate(4, java.sql.Date.valueOf(member.getJoinDate()));
            pstmt.setString(5, member.getStatus().toString());

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                try (ResultSet keys = pstmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        member.setMemberId(keys.getInt(1));
                    }
                }
                return true;
            } else  {
                return false;
            }

        } catch (SQLException e) {
            System.out.println("Error adding member: " + e.getMessage());
            return false;
        }
    }

    public List<Member> getAllMembers() {
        List<Member> members = new ArrayList<>();
        String sql = "SELECT * FROM members";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                members.add(mapRowToMember(rs));
            }

        } catch (SQLException e) {
            System.out.println("Error loading members: " + e.getMessage());
        }

        return members;
    }

    public Member getMemberById(int id) {
        String sql = "SELECT * FROM members WHERE member_id = ?";
        try (Connection conn = DBConnection.getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql);) {

            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapRowToMember(rs);
                }
            }

        } catch (SQLException e) {
            System.out.println("Error loading member: " + e.getMessage());
        }
        return null;
    }

    public boolean updateMember(Member member) {
        String sql = "UPDATE members " + "SET name=?, email=?, phone=?, join_date=?, status=? WHERE member_id = ?";
        try (Connection conn = DBConnection.getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql);) {

            pstmt.setString(1,  member.getName());
            pstmt.setString(2, member.getEmail());
            pstmt.setString(3, member.getPhone());
            pstmt.setDate(4, java.sql.Date.valueOf(member.getJoinDate()));
            pstmt.setString(5, member.getStatus().toString());
            pstmt.setInt(6, member.getMemberId());
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            System.out.println("Error updating member: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteMember(int id) {
        String sql = "DELETE FROM members WHERE member_id = ?";
        try (Connection conn = DBConnection.getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql);) {
            pstmt.setInt(1, id);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.out.println("Error deleting member: " + e.getMessage());
            return false;
        }
    }

    private Member mapRowToMember(ResultSet rs) throws SQLException {
        return new Member(
                rs.getInt("member_id"),
                rs.getString("name"),
                rs.getString("email"),
                rs.getDate("join_date").toLocalDate(),
                rs.getString("phone"),
                Member.Status.valueOf(rs.getString("status"))
        );
    }

    public static void main(String[] args) {
        MemberDAO dao = new MemberDAO();
        Member spongebob = new Member("Spongebob", "sqrpnts@liquimail.bubble", LocalDate.of(2026, 3, 2), "777-333");
        dao.addMember(spongebob);
        System.out.println("Spongebob's generated ID: " + spongebob.getMemberId());

        System.out.println("Fetching Spongebob's details: ");
        Member member = dao.getMemberById(spongebob.getMemberId());  // use the real ID, not a guess
        if (member == null) {
            System.out.println("Failed to fetch Spongebob — aborting test.");
            return;
        }
        System.out.println(member);

        System.out.println("Testing update function: ");
        member.setJoinDate(LocalDate.of(2001, 1, 1));
        member.setStatus(Member.Status.SUSPENDED);
        if (dao.updateMember(member)) {
            System.out.println("Updated: " + dao.getMemberById(member.getMemberId()));
        }

        if (dao.deleteMember(member.getMemberId())) {
            System.out.println("Spongebob deleted successfully.");
        }
    }
}
