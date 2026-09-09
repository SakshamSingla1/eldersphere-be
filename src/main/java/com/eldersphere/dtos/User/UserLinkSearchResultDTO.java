package com.eldersphere.dtos.User;

import com.eldersphere.enums.UserTypeEnum;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserLinkSearchResultDTO {
    private Long id;
    private String fullName;
    private String maskedEmail;
    private String maskedPhone;
    private UserTypeEnum userType;
}
