package com.eldersphere.services.impl;

import com.eldersphere.dao.service.ServiceOfferingDao;
import com.eldersphere.dtos.ServiceOffering.ServiceOfferingRequest;
import com.eldersphere.dtos.ServiceOffering.ServiceOfferingResponse;
import com.eldersphere.entities.ServiceOffering;
import com.eldersphere.enums.ExceptionCodeEnum;
import com.eldersphere.enums.ServiceCategoryEnum;
import com.eldersphere.exceptions.GenericException;
import com.eldersphere.services.ServiceOfferingService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ServiceOfferingServiceImpl implements ServiceOfferingService {

    private final ServiceOfferingDao serviceOfferingDao;

    @Override
    @Transactional
    public ServiceOfferingResponse create(ServiceOfferingRequest request) throws GenericException {
        ServiceOffering offering = ServiceOffering.builder()
                .name(request.getName())
                .category(request.getCategory())
                .description(request.getDescription())
                .basePrice(request.getBasePrice())
                .durationMinutes(request.getDurationMinutes())
                .build();
        return toResponse(serviceOfferingDao.save(offering));
    }

    @Override
    @Transactional
    public ServiceOfferingResponse update(Long id, ServiceOfferingRequest request) throws GenericException {
        ServiceOffering offering = serviceOfferingDao.findById(id, true);
        if (offering == null) {
            throw new GenericException(ExceptionCodeEnum.SERVICE_OFFERING_NOT_FOUND, "Service offering not found");
        }
        offering.setName(request.getName());
        offering.setCategory(request.getCategory());
        offering.setDescription(request.getDescription());
        offering.setBasePrice(request.getBasePrice());
        offering.setDurationMinutes(request.getDurationMinutes());
        return toResponse(serviceOfferingDao.save(offering));
    }

    @Override
    public ServiceOfferingResponse getById(Long id) throws GenericException {
        ServiceOffering offering = serviceOfferingDao.findById(id, true);
        if (offering == null) {
            throw new GenericException(ExceptionCodeEnum.SERVICE_OFFERING_NOT_FOUND, "Service offering not found");
        }
        return toResponse(offering);
    }

    @Override
    public Page<ServiceOfferingResponse> getAll(ServiceCategoryEnum category, Pageable pageable) {
        Page<ServiceOffering> page = category != null
                ? serviceOfferingDao.findByCategory(category, pageable)
                : serviceOfferingDao.getRepository().findAll(pageable);
        return page.map(this::toResponse);
    }

    @Override
    @Transactional
    public void delete(Long id) throws GenericException {
        ServiceOffering offering = serviceOfferingDao.findById(id, true);
        if (offering == null) {
            throw new GenericException(ExceptionCodeEnum.SERVICE_OFFERING_NOT_FOUND, "Service offering not found");
        }
        serviceOfferingDao.deleteById(id);
    }

    private ServiceOfferingResponse toResponse(ServiceOffering offering) {
        ServiceOfferingResponse response = new ServiceOfferingResponse();
        response.setId(offering.getId());
        response.setName(offering.getName());
        response.setCategory(offering.getCategory());
        response.setDescription(offering.getDescription());
        response.setBasePrice(offering.getBasePrice());
        response.setDurationMinutes(offering.getDurationMinutes());
        response.setCreatedAt(offering.getCreatedAt());
        response.setUpdatedAt(offering.getUpdatedAt());
        return response;
    }
}
