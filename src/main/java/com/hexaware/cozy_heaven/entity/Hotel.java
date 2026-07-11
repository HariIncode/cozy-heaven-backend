package com.hexaware.cozy_heaven.entity;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "hotels")
public class Hotel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "hotel_id")
    private Integer hotelId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    @JsonBackReference("user-hotels")
    private User owner;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 100)
    private String location;


    @Column(length = 500)
    private String description;

    @Column(name = "image_url")
    private String imageUrl;


    private boolean dining;
    private boolean parking;
    private boolean wifi;

    @Column(name = "room_service")
    private boolean roomService;

    private boolean pool;
    private boolean gym;

    @Column(name = "average_rating", nullable = false)
    private double averageRating = 0.0;

    @Column(name = "total_reviews", nullable = false)
    private int totalReviews = 0;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
    }


    @OneToMany(mappedBy = "hotel", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("hotel-rooms")
    private List<Room> rooms;


    @OneToMany(mappedBy = "hotel", fetch = FetchType.LAZY)
    @JsonManagedReference("hotel-reviews")
    private List<Review> reviews;
}