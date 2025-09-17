package com.pragma.bootcamp.model.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanApprovedEvent {
    private String approvedAmount;
    private Integer newTotalCount;
    private String newTotalAmount;
}
