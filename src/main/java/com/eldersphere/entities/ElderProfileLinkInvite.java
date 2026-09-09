package com.eldersphere.entities;

import com.eldersphere.audit.Auditable;
import com.eldersphere.enums.LinkInviteStatusEnum;
import com.eldersphere.enums.UserTypeEnum;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * A pending (or resolved) request to link an existing user to an ElderProfile — either as the
 * elder themselves (invitedRole = ELDER, sets ElderProfile.elderUserId on accept) or as an
 * additional co-managing family member (invitedRole = FAMILY_MEMBER, creates a FamilyElderLink
 * row on accept). The invited user must explicitly accept before any link is established.
 */
@Entity
@Table(name = "elder_profile_link_invites")
@Data
@EqualsAndHashCode(callSuper = false)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ElderProfileLinkInvite extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "elder_profile_id", nullable = false)
    private Long elderProfileId;

    @Column(name = "invited_user_id", nullable = false)
    private Long invitedUserId;

    @Enumerated(EnumType.STRING)
    @Column(name = "invited_role", nullable = false)
    private UserTypeEnum invitedRole;

    @Column(name = "invited_by_user_id", nullable = false)
    private Long invitedByUserId;

    @Column(name = "relationship_label")
    private String relationshipLabel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LinkInviteStatusEnum status;

    @Column(name = "responded_at")
    private LocalDateTime respondedAt;
}
