package com.hexaware.cozy_heaven.entity;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.hexaware.cozy_heaven.model.RefundStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "refunds")
public class Refund {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "refund_id")
    private Long refundId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", unique = true, nullable = false)
    @JsonBackReference("booking-refund")
    private Booking booking;


    @Column(name = "refund_percentage", nullable = false)
    private Integer refundPercentage;


    @Column(name = "refund_amount", nullable = false)
    private Integer refundAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "refund_status", nullable = false)
    private RefundStatus refundStatus = RefundStatus.REQUESTED;


    @Column(length = 500)
    private String reason;


    @Column(name = "processed_at")
    private LocalDateTime processedAt;


    @Column(name = "refunded_at", updatable = false)
    private LocalDateTime refundedAt;

    @PrePersist
    public void onCreate() {
        this.refundedAt = LocalDateTime.now();
    }
}