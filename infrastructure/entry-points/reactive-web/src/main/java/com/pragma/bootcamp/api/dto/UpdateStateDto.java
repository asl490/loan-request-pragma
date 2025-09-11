package com.pragma.bootcamp.api.dto;

import com.pragma.bootcamp.model.requeststatus.Status;
import lombok.Data;

@Data
public class UpdateStateDto {
    private Status state;
}
