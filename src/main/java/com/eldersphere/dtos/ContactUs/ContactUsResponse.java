package com.eldersphere.dtos.ContactUs;

import com.eldersphere.dtos.Common.AuditableResponse;
import com.eldersphere.enums.ContactUsStatusEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ContactUsResponse extends AuditableResponse {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private String message;
    private ContactUsStatusEnum status;
}
