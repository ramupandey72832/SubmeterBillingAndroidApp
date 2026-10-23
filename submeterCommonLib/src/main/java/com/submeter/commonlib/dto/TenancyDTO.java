package com.submeter.commonlib.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TenancyDTO {
    private int id;
    private int roomId;
    private int tenantId;
    private String StartDate;
    private String endDate;
}
