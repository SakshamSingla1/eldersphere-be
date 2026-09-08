package com.eldersphere.services.impl;

import com.eldersphere.dao.landing.LandingFaqDao;
import com.eldersphere.dao.landing.LandingFeatureDao;
import com.eldersphere.dao.landing.LandingPageConfigDao;
import com.eldersphere.dao.landing.LandingTestimonialDao;
import com.eldersphere.dtos.Landing.*;
import com.eldersphere.entities.LandingFaq;
import com.eldersphere.entities.LandingFeature;
import com.eldersphere.entities.LandingPageConfig;
import com.eldersphere.entities.LandingTestimonial;
import com.eldersphere.enums.ExceptionCodeEnum;
import com.eldersphere.exceptions.GenericException;
import com.eldersphere.services.LandingPageService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LandingPageServiceImpl implements LandingPageService {

    private final LandingPageConfigDao landingPageConfigDao;
    private final LandingFeatureDao landingFeatureDao;
    private final LandingFaqDao landingFaqDao;
    private final LandingTestimonialDao landingTestimonialDao;

    @Override
    @Cacheable("landingPagePublic")
    public LandingPageResponse getPublicPage() {
        LandingPageConfig config = landingPageConfigDao.findFirst();
        return LandingPageResponse.builder()
                .config(config != null ? toConfigResponse(config) : null)
                .features(landingFeatureDao.findActive().stream().map(this::toFeatureResponse).collect(Collectors.toList()))
                .faqs(landingFaqDao.findActive().stream().map(this::toFaqResponse).collect(Collectors.toList()))
                .testimonials(landingTestimonialDao.findActive().stream().map(this::toTestimonialResponse).collect(Collectors.toList()))
                .build();
    }

    @Override
    @Transactional
    @CacheEvict(value = "landingPagePublic", allEntries = true)
    public LandingPageConfigResponse updateConfig(LandingPageConfigRequest request) {
        LandingPageConfig config = landingPageConfigDao.findFirst();
        if (config == null) {
            config = new LandingPageConfig();
        }
        config.setHeroHeadline(request.getHeroHeadline());
        config.setHeroSubheadline(request.getHeroSubheadline());
        config.setHeroImageUrl(request.getHeroImageUrl());
        config.setCtaHeadline(request.getCtaHeadline());
        config.setCtaDescription(request.getCtaDescription());
        config.setCtaButtonText(request.getCtaButtonText());
        return toConfigResponse(landingPageConfigDao.save(config));
    }

    @Override
    @Transactional
    @CacheEvict(value = "landingPagePublic", allEntries = true)
    public LandingFeatureResponse createFeature(LandingFeatureRequest request) {
        LandingFeature feature = LandingFeature.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .iconName(request.getIconName())
                .sortOrder(request.getSortOrder())
                .isActive(request.isActive())
                .build();
        return toFeatureResponse(landingFeatureDao.save(feature));
    }

    @Override
    public List<LandingFeatureResponse> getAllFeatures() {
        return landingFeatureDao.findAll().stream().map(this::toFeatureResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    @CacheEvict(value = "landingPagePublic", allEntries = true)
    public LandingFeatureResponse updateFeature(Long id, LandingFeatureRequest request) throws GenericException {
        LandingFeature feature = landingFeatureDao.findById(id, true);
        if (feature == null) {
            throw new GenericException(ExceptionCodeEnum.LANDING_FEATURE_NOT_FOUND, "Landing feature not found");
        }
        feature.setTitle(request.getTitle());
        feature.setDescription(request.getDescription());
        feature.setIconName(request.getIconName());
        feature.setSortOrder(request.getSortOrder());
        feature.setActive(request.isActive());
        return toFeatureResponse(landingFeatureDao.save(feature));
    }

    @Override
    @Transactional
    @CacheEvict(value = "landingPagePublic", allEntries = true)
    public void deleteFeature(Long id) throws GenericException {
        LandingFeature feature = landingFeatureDao.findById(id, true);
        if (feature == null) {
            throw new GenericException(ExceptionCodeEnum.LANDING_FEATURE_NOT_FOUND, "Landing feature not found");
        }
        landingFeatureDao.deleteById(id);
    }

    @Override
    @Transactional
    @CacheEvict(value = "landingPagePublic", allEntries = true)
    public LandingFaqResponse createFaq(LandingFaqRequest request) {
        LandingFaq faq = LandingFaq.builder()
                .question(request.getQuestion())
                .answer(request.getAnswer())
                .sortOrder(request.getSortOrder())
                .isActive(request.isActive())
                .build();
        return toFaqResponse(landingFaqDao.save(faq));
    }

    @Override
    public List<LandingFaqResponse> getAllFaqs() {
        return landingFaqDao.findAll().stream().map(this::toFaqResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    @CacheEvict(value = "landingPagePublic", allEntries = true)
    public LandingFaqResponse updateFaq(Long id, LandingFaqRequest request) throws GenericException {
        LandingFaq faq = landingFaqDao.findById(id, true);
        if (faq == null) {
            throw new GenericException(ExceptionCodeEnum.LANDING_FAQ_NOT_FOUND, "Landing FAQ not found");
        }
        faq.setQuestion(request.getQuestion());
        faq.setAnswer(request.getAnswer());
        faq.setSortOrder(request.getSortOrder());
        faq.setActive(request.isActive());
        return toFaqResponse(landingFaqDao.save(faq));
    }

    @Override
    @Transactional
    @CacheEvict(value = "landingPagePublic", allEntries = true)
    public void deleteFaq(Long id) throws GenericException {
        LandingFaq faq = landingFaqDao.findById(id, true);
        if (faq == null) {
            throw new GenericException(ExceptionCodeEnum.LANDING_FAQ_NOT_FOUND, "Landing FAQ not found");
        }
        landingFaqDao.deleteById(id);
    }

    @Override
    @Transactional
    @CacheEvict(value = "landingPagePublic", allEntries = true)
    public LandingTestimonialResponse createTestimonial(LandingTestimonialRequest request) {
        LandingTestimonial testimonial = LandingTestimonial.builder()
                .authorName(request.getAuthorName())
                .authorRole(request.getAuthorRole())
                .content(request.getContent())
                .avatarUrl(request.getAvatarUrl())
                .rating(request.getRating())
                .sortOrder(request.getSortOrder())
                .isActive(request.isActive())
                .build();
        return toTestimonialResponse(landingTestimonialDao.save(testimonial));
    }

    @Override
    public List<LandingTestimonialResponse> getAllTestimonials() {
        return landingTestimonialDao.findAll().stream().map(this::toTestimonialResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    @CacheEvict(value = "landingPagePublic", allEntries = true)
    public LandingTestimonialResponse updateTestimonial(Long id, LandingTestimonialRequest request) throws GenericException {
        LandingTestimonial testimonial = landingTestimonialDao.findById(id, true);
        if (testimonial == null) {
            throw new GenericException(ExceptionCodeEnum.LANDING_TESTIMONIAL_NOT_FOUND, "Landing testimonial not found");
        }
        testimonial.setAuthorName(request.getAuthorName());
        testimonial.setAuthorRole(request.getAuthorRole());
        testimonial.setContent(request.getContent());
        testimonial.setAvatarUrl(request.getAvatarUrl());
        testimonial.setRating(request.getRating());
        testimonial.setSortOrder(request.getSortOrder());
        testimonial.setActive(request.isActive());
        return toTestimonialResponse(landingTestimonialDao.save(testimonial));
    }

    @Override
    @Transactional
    @CacheEvict(value = "landingPagePublic", allEntries = true)
    public void deleteTestimonial(Long id) throws GenericException {
        LandingTestimonial testimonial = landingTestimonialDao.findById(id, true);
        if (testimonial == null) {
            throw new GenericException(ExceptionCodeEnum.LANDING_TESTIMONIAL_NOT_FOUND, "Landing testimonial not found");
        }
        landingTestimonialDao.deleteById(id);
    }

    private LandingPageConfigResponse toConfigResponse(LandingPageConfig config) {
        return LandingPageConfigResponse.builder()
                .id(config.getId())
                .heroHeadline(config.getHeroHeadline())
                .heroSubheadline(config.getHeroSubheadline())
                .heroImageUrl(config.getHeroImageUrl())
                .ctaHeadline(config.getCtaHeadline())
                .ctaDescription(config.getCtaDescription())
                .ctaButtonText(config.getCtaButtonText())
                .build();
    }

    private LandingFeatureResponse toFeatureResponse(LandingFeature feature) {
        return LandingFeatureResponse.builder()
                .id(feature.getId())
                .title(feature.getTitle())
                .description(feature.getDescription())
                .iconName(feature.getIconName())
                .sortOrder(feature.getSortOrder())
                .isActive(feature.isActive())
                .build();
    }

    private LandingFaqResponse toFaqResponse(LandingFaq faq) {
        return LandingFaqResponse.builder()
                .id(faq.getId())
                .question(faq.getQuestion())
                .answer(faq.getAnswer())
                .sortOrder(faq.getSortOrder())
                .isActive(faq.isActive())
                .build();
    }

    private LandingTestimonialResponse toTestimonialResponse(LandingTestimonial testimonial) {
        return LandingTestimonialResponse.builder()
                .id(testimonial.getId())
                .authorName(testimonial.getAuthorName())
                .authorRole(testimonial.getAuthorRole())
                .content(testimonial.getContent())
                .avatarUrl(testimonial.getAvatarUrl())
                .rating(testimonial.getRating())
                .sortOrder(testimonial.getSortOrder())
                .isActive(testimonial.isActive())
                .build();
    }
}
