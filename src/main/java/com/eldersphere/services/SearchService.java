package com.eldersphere.services;

import com.eldersphere.dtos.Search.CaretakerSearchResultDTO;
import com.eldersphere.enums.CaretakerVerificationStatusEnum;
import com.eldersphere.enums.ServiceCategoryEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;

public interface SearchService {
    Page<CaretakerSearchResultDTO> searchCaretakers(ServiceCategoryEnum category, Double minRating,
                                                      CaretakerVerificationStatusEnum verificationStatus,
                                                      BigDecimal minRate, BigDecimal maxRate, String location,
                                                      Pageable pageable);
}
