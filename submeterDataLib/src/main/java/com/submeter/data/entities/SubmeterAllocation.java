package com.submeter.data.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubmeterAllocation {
    private int id;
    private int meterId;
    private int roomId;
    private String startingDate;
    private String endingDate;
}
