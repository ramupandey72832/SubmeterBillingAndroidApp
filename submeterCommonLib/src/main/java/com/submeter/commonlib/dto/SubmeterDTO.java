package com.submeter.commonlib.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubmeterDTO {
    private int id;
    private String meterSerialNumber;
    private long initialReading;
    private String note;
}
