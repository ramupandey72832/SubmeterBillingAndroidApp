package com.submeter.commonlib.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubmeterAllocationDTO {
    private int id;
    private int meterId;
    private int roomId;
    private String startingDate;
    private String endingDate;
}
