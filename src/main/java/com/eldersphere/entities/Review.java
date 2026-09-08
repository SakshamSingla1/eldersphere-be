package com.eldersphere.entities;

import com.eldersphere.audit.Auditable;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "reviews", uniqueConstraints = @UniqueConstraint(name = "uk_review_booking", columnNames = {"booking_id"}))
@Data
@EqualsAndHashCode(callSuper = false)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Review extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "booking_id", nullable = false)
    private Long bookingId;

    @Column(name = "reviewer_id", nullable = false)
    private Long reviewerId;

    @Column(name = "caretaker_id", nullable = false)
    private Long caretakerId;

    @Column(nullable = false)
    private Integer rating;

    @Column(columnDefinition = "TEXT")
    private String comment;

    @Column(name = "punctuality_rating")
    private Integer punctualityRating;

    @Column(name = "care_quality_rating")
    private Integer careQualityRating;

    @Column(name = "communication_rating")
    private Integer communicationRating;

    @Column(name = "photo_file_asset_id")
    private Long photoFileAssetId;
}
