package com.eldersphere.services;

import com.eldersphere.dtos.ContactUs.ContactUsRequest;
import com.eldersphere.dtos.ContactUs.ContactUsResponse;
import com.eldersphere.enums.ContactUsStatusEnum;
import com.eldersphere.exceptions.GenericException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ContactUsService {
    ContactUsResponse create(ContactUsRequest request);
    Page<ContactUsResponse> search(String search, ContactUsStatusEnum status, Pageable pageable);
    ContactUsResponse updateStatus(Long id, ContactUsStatusEnum status) throws GenericException;
    void delete(Long id) throws GenericException;
}
