package com.pragma.bootcamp.r2dbc.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table(name = "State")
@AllArgsConstructor()
@NoArgsConstructor
@Data
@Builder
public class RequestStatusEntity {
    @Id
    private Long id;
    private String name;
    private String description;
}
