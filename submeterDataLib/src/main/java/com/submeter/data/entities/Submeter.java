package com.submeter.data.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Submeter {
    private int id;
    private String meterSerialNumber;
    private long initialReading;
    private String note;
}


