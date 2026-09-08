package com.eldersphere.dao.contactus;

import com.eldersphere.dao.IDao;
import com.eldersphere.entities.ContactUs;
import com.eldersphere.enums.ContactUsStatusEnum;
import com.eldersphere.repositories.ContactUsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ContactUsDao implements IDao<ContactUs, Long> {

    private final ContactUsRepository contactUsRepository;

    @Override
    public JpaRepository<ContactUs, Long> getRepository() {
        return contactUsRepository;
    }

    public ContactUs save(ContactUs contactUs) {
        return contactUsRepository.save(contactUs);
    }

    public Page<ContactUs> findByCriteria(String search, ContactUsStatusEnum status, Pageable pageable) {
        return contactUsRepository.findByCriteria(search, status, pageable);
    }

    public void deleteById(Long id) {
        contactUsRepository.deleteById(id);
    }
}
