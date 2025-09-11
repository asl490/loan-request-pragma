package com.pragma.bootcamp.model.requeststatus;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder(toBuilder = true)
public class RequestStatus {
    private Long id;
    private String name;
    private String description;

}
