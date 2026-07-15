package com.boogle.dto.request;

import com.boogle.entity.type.ReportReason;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ReportRequestDto {
    private ReportReason reason;
    private String detail;
}
