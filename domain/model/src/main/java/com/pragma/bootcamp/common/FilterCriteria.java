package com.pragma.bootcamp.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FilterCriteria {
    private String field;
    private String operator; // eq, ne, gt, lt, gte, lte, like, in
    private Object value;
    private String logicalOperator; // AND, OR
}
