package com.eldersphere.entities;

import com.eldersphere.audit.Auditable;
import com.eldersphere.enums.MedicalRecordTypeEnum;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "medical_records")
@Data
@EqualsAndHashCode(callSuper = false)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedicalRecord extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "elder_profile_id", nullable = false)
    private Long elderProfileId;

    @Enumerated(EnumType.STRING)
    private MedicalRecordTypeEnum type;

    private String title;

    @Column(name = "document_file_asset_id")
    private Long documentFileAssetId;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "shared_with_family")
    private boolean sharedWithFamily;
}
