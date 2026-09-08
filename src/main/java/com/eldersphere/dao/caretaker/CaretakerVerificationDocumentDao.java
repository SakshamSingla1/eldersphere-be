package com.eldersphere.dao.caretaker;

import com.eldersphere.dao.IDao;
import com.eldersphere.entities.CaretakerVerificationDocument;
import com.eldersphere.repositories.CaretakerVerificationDocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class CaretakerVerificationDocumentDao implements IDao<CaretakerVerificationDocument, Long> {

    private final CaretakerVerificationDocumentRepository caretakerVerificationDocumentRepository;

    @Override
    public JpaRepository<CaretakerVerificationDocument, Long> getRepository() {
        return caretakerVerificationDocumentRepository;
    }

    public CaretakerVerificationDocument save(CaretakerVerificationDocument document) {
        return caretakerVerificationDocumentRepository.save(document);
    }

    public List<CaretakerVerificationDocument> findByCaretakerProfileId(Long caretakerProfileId) {
        return caretakerVerificationDocumentRepository.findByCaretakerProfileIdOrderByCreatedAtDesc(caretakerProfileId);
    }
}
