package com.eldersphere.services.impl;

import com.eldersphere.dao.contactus.ContactUsDao;
import com.eldersphere.dtos.ContactUs.ContactUsRequest;
import com.eldersphere.dtos.ContactUs.ContactUsResponse;
import com.eldersphere.entities.ContactUs;
import com.eldersphere.enums.ContactUsStatusEnum;
import com.eldersphere.enums.ExceptionCodeEnum;
import com.eldersphere.exceptions.GenericException;
import com.eldersphere.services.ContactUsService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ContactUsServiceImpl implements ContactUsService {

    private final ContactUsDao contactUsDao;

    @Override
    @Transactional
    public ContactUsResponse create(ContactUsRequest request) {
        ContactUs contactUs = ContactUs.builder()
                .name(request.getName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .message(request.getMessage())
                .status(ContactUsStatusEnum.NEW)
                .build();
        return toResponse(contactUsDao.save(contactUs));
    }

    @Override
    public Page<ContactUsResponse> search(String search, ContactUsStatusEnum status, Pageable pageable) {
        return contactUsDao.findByCriteria(search, status, pageable).map(this::toResponse);
    }

    @Override
    @Transactional
    public ContactUsResponse updateStatus(Long id, ContactUsStatusEnum status) throws GenericException {
        ContactUs contactUs = contactUsDao.findById(id, true);
        if (contactUs == null) {
            throw new GenericException(ExceptionCodeEnum.CONTACT_US_NOT_FOUND, "Contact request not found");
        }
        contactUs.setStatus(status);
        return toResponse(contactUsDao.save(contactUs));
    }

    @Override
    @Transactional
    public void delete(Long id) throws GenericException {
        ContactUs contactUs = contactUsDao.findById(id, true);
        if (contactUs == null) {
            throw new GenericException(ExceptionCodeEnum.CONTACT_US_NOT_FOUND, "Contact request not found");
        }
        contactUsDao.deleteById(id);
    }

    private ContactUsResponse toResponse(ContactUs contactUs) {
        ContactUsResponse response = new ContactUsResponse();
        response.setId(contactUs.getId());
        response.setName(contactUs.getName());
        response.setEmail(contactUs.getEmail());
        response.setPhone(contactUs.getPhone());
        response.setMessage(contactUs.getMessage());
        response.setStatus(contactUs.getStatus());
        response.setCreatedAt(contactUs.getCreatedAt());
        response.setUpdatedAt(contactUs.getUpdatedAt());
        return response;
    }
}
