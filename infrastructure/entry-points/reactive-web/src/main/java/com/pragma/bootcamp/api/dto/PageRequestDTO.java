package com.pragma.bootcamp.api.dto;

import com.pragma.bootcamp.common.FilterCriteria;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor

public class PageRequestDTO {
    @Builder.Default
    private int page = 0;
    @Builder.Default
    private int size = 10;
    //    private String sortBy;
//    private String sortDirection = "ASC";
    private List<FilterCriteria> filters;
}
