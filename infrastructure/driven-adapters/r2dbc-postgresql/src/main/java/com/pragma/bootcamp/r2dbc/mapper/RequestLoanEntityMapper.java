package com.pragma.bootcamp.r2dbc.mapper;

import com.pragma.bootcamp.model.loantype.LoanType;
import com.pragma.bootcamp.model.requestloan.RequestLoan;
import com.pragma.bootcamp.model.requeststatus.RequestStatus;
import com.pragma.bootcamp.r2dbc.entity.RequestLoanEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = "spring")
public interface RequestLoanEntityMapper {

    // Dominio → Entidad
    @Mapping(target = "loanType", source = "loanType.id")
    @Mapping(target = "requestStatus", source = "requestStatus.id")
    RequestLoanEntity toEntity(RequestLoan requestLoan);

    // Entidad → Dominio
    @Mapping(target = "loanType", source = "loanType", qualifiedByName = "loanTypeFromId")
    @Mapping(target = "requestStatus", source = "requestStatus", qualifiedByName = "requestStatusFromId")
    RequestLoan toDomain(RequestLoanEntity entity);

    List<RequestLoan> toDomainList(List<RequestLoanEntity> entities);

    List<RequestLoanEntity> toEntityList(List<RequestLoan> loans);

    // Métodos auxiliares para construir objetos a partir de IDs
    @Named("loanTypeFromId")
    default LoanType mapLoanType(Long id) {
        return id == null ? null : LoanType.builder().id(id).build();
    }

    @Named("requestStatusFromId")
    default RequestStatus mapRequestStatus(Long id) {
        return id == null ? null : RequestStatus.builder().id(id).build();
    }
}