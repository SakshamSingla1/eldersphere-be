package com.eldersphere.entities;

import com.eldersphere.audit.Auditable;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "landing_page_config")
@Data
@EqualsAndHashCode(callSuper = false)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LandingPageConfig extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "hero_headline")
    private String heroHeadline;

    @Column(name = "hero_subheadline", columnDefinition = "TEXT")
    private String heroSubheadline;

    @Column(name = "hero_image_url")
    private String heroImageUrl;

    @Column(name = "cta_headline")
    private String ctaHeadline;

    @Column(name = "cta_description", columnDefinition = "TEXT")
    private String ctaDescription;

    @Column(name = "cta_button_text")
    private String ctaButtonText;
}
