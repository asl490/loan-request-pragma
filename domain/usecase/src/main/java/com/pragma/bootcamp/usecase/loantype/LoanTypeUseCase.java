package com.pragma.bootcamp.usecase.loantype;

import com.pragma.bootcamp.exception.BusinessException;
import com.pragma.bootcamp.model.loantype.LoanType;
import com.pragma.bootcamp.model.loantype.gateways.LoanTypeRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class LoanTypeUseCase {

    private final LoanTypeRepository loanTypeRepository;

    public Mono<LoanType> getTypeLoanById(Long id) {
        return loanTypeRepository.findById(id)
                .switchIfEmpty(Mono.error(new BusinessException(BusinessException.Type.REQUEST_LOAN_NOT_FOUND)));
    }
}
