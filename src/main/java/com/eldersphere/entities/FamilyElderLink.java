package com.eldersphere.entities;

import com.eldersphere.audit.Auditable;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * An active co-management link between a FAMILY_MEMBER user and an ElderProfile they did not
 * originally create. ElderProfile.familyUserId remains the profile's original owner; rows here
 * are *additional* family members who accepted an invite to share management of the same elder.
 * Removal is a hard delete — there is no "revoked" state to track once a link is active (see
 * ElderProfileLinkInvite for the pending/declined/revoked lifecycle that precedes this row).
 */
@Entity
@Table(name = "family_elder_links")
@Data
@EqualsAndHashCode(callSuper = false)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FamilyElderLink extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "elder_profile_id", nullable = false)
    private Long elderProfileId;

    @Column(name = "family_user_id", nullable = false)
    private Long familyUserId;

    @Column(name = "relationship_label")
    private String relationshipLabel;

    @Column(name = "invited_by_user_id")
    private Long invitedByUserId;
}
