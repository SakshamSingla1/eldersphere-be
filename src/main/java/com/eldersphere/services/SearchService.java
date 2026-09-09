package com.eldersphere.services;

import com.eldersphere.dtos.Search.CaretakerSearchResultDTO;
import com.eldersphere.dtos.User.UserLinkSearchResultDTO;
import com.eldersphere.entities.User;
import com.eldersphere.enums.CaretakerVerificationStatusEnum;
import com.eldersphere.enums.ServiceCategoryEnum;
import com.eldersphere.enums.UserTypeEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

public interface SearchService {
    Page<CaretakerSearchResultDTO> searchCaretakers(ServiceCategoryEnum category, Double minRating,
                                                      CaretakerVerificationStatusEnum verificationStatus,
                                                      BigDecimal minRate, BigDecimal maxRate, String location,
                                                      Pageable pageable);

    List<UserLinkSearchResultDTO> searchLinkableUsers(String query, UserTypeEnum userType, User caller);
}
