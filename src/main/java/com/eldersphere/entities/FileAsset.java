package com.eldersphere.entities;

import com.eldersphere.audit.Auditable;
import com.eldersphere.enums.ResourceTypeEnum;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "file_assets")
@Data
@EqualsAndHashCode(callSuper = false)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileAsset extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Public URL the file can be downloaded from (served off the local uploads/ dir). */
    private String url;

    /** Path on local disk, relative to the configured upload directory. */
    private String path;

    @Column(name = "file_name")
    private String fileName;

    @Column(name = "file_type")
    private String fileType;

    @Enumerated(EnumType.STRING)
    @Column(name = "resource_type")
    private ResourceTypeEnum resourceType;

    @Column(name = "uploaded_by")
    private Long uploadedBy;
}
