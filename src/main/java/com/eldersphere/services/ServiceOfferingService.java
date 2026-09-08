package com.eldersphere.services;

import com.eldersphere.dtos.ServiceOffering.ServiceOfferingRequest;
import com.eldersphere.dtos.ServiceOffering.ServiceOfferingResponse;
import com.eldersphere.enums.ServiceCategoryEnum;
import com.eldersphere.exceptions.GenericException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ServiceOfferingService {
    ServiceOfferingResponse create(ServiceOfferingRequest request) throws GenericException;
    ServiceOfferingResponse update(Long id, ServiceOfferingRequest request) throws GenericException;
    ServiceOfferingResponse getById(Long id) throws GenericException;
    Page<ServiceOfferingResponse> getAll(ServiceCategoryEnum category, Pageable pageable);
    void delete(Long id) throws GenericException;
}
