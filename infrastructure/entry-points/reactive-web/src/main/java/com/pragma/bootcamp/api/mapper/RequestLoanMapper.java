package com.pragma.bootcamp.api.mapper;

import com.pragma.bootcamp.api.dto.RequestLoanCreateDTO;
import com.pragma.bootcamp.api.dto.RequestLoanDTO;
import com.pragma.bootcamp.model.loantype.LoanType;
import com.pragma.bootcamp.model.requestloan.RequestLoan;
import com.pragma.bootcamp.model.requeststatus.RequestStatus;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = "spring")
public interface RequestLoanMapper {

    @Mapping(target = "loanType", source = "loanType.id")
    @Mapping(target = "requestStatus", source = "requestStatus.id")
    RequestLoanDTO toDTO(RequestLoan requestLoan);

    @Mapping(target = "loanType", source = "loanType", qualifiedByName = "loanTypeFromId")
//    @Mapping(target = "requestStatus", source = "requestStatus", qualifiedByName = "requestStatusFromId")
    RequestLoan toDomain(RequestLoanCreateDTO requestLoanCreateDTO);

    List<RequestLoanDTO> toEntityList(List<RequestLoan> loans);

    @Named("loanTypeFromId")
    default LoanType mapLoanType(Long id) {
        return id == null ? null : LoanType.builder().id(id).build();
    }

    @Named("requestStatusFromId")
    default RequestStatus mapRequestStatus(Long id) {
        return id == null ? null : RequestStatus.builder().id(id).build();
    }
}