package com.eldersphere.entities;

import com.eldersphere.audit.Auditable;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Join row linking a {@link CaretakerProfile} to a {@link FileAsset} the caretaker submitted
 * as verification evidence (ID, certification, background-check document, etc.). An admin
 * reviewing a profile's verification status lists these to see what was actually submitted,
 * rather than trusting a bare status flag.
 */
@Entity
@Table(name = "caretaker_verification_documents")
@Data
@EqualsAndHashCode(callSuper = false)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CaretakerVerificationDocument extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "caretaker_profile_id", nullable = false)
    private Long caretakerProfileId;

    @Column(name = "file_asset_id", nullable = false)
    private Long fileAssetId;

    @Column(name = "uploaded_by")
    private Long uploadedBy;
}
