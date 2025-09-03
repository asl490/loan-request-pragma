package com.pragma.bootcamp.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PageRequest {
    private int page;
    private int size;
    //    private String sortBy;
//    private String sortDirection; // ASC, DESC
    private List<FilterCriteria> filters;
}