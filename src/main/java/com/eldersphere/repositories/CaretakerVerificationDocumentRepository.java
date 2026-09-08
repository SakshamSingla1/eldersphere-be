package com.eldersphere.repositories;

import com.eldersphere.entities.CaretakerVerificationDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CaretakerVerificationDocumentRepository extends JpaRepository<CaretakerVerificationDocument, Long> {

    List<CaretakerVerificationDocument> findByCaretakerProfileIdOrderByCreatedAtDesc(Long caretakerProfileId);
}
