package com.eldersphere.dtos.Messaging;

import lombok.Data;

@Data
public class MessageRequest {

    private String content;

    /** Optional: id of a previously uploaded file (via /api/v1/files) to attach to this message. */
    private Long fileAssetId;
}
