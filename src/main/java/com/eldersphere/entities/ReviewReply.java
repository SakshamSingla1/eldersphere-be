package com.eldersphere.entities;

import com.eldersphere.audit.Auditable;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "review_replies", uniqueConstraints = @UniqueConstraint(name = "uk_review_reply_review", columnNames = {"review_id"}))
@Data
@EqualsAndHashCode(callSuper = false)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewReply extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "review_id", nullable = false)
    private Long reviewId;

    @Column(name = "caretaker_id", nullable = false)
    private Long caretakerId;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;
}
