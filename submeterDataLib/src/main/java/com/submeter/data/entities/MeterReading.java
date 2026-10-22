package com.submeter.data.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MeterReading {
    private int id;
    private int meterId;
    private long readingValue;
    private String date;
    private String imageUrlOrPath;
}
