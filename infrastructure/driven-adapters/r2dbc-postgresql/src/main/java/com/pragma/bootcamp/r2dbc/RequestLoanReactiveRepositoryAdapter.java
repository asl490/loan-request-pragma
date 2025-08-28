package com.pragma.bootcamp.r2dbc;

import org.reactivecommons.utils.ObjectMapper;
import org.springframework.stereotype.Repository;

import com.pragma.bootcamp.model.requestloan.RequestLoan;
import com.pragma.bootcamp.r2dbc.entity.RequestLoanEntity;
import com.pragma.bootcamp.r2dbc.helper.ReactiveAdapterOperations;

@Repository
public class RequestLoanReactiveRepositoryAdapter extends
        ReactiveAdapterOperations<RequestLoan/* change for domain model */, RequestLoanEntity/*
                                                                                              * change for adapter model
                                                                                              */, Long, RequestLoanReactiveRepository> {
    public RequestLoanReactiveRepositoryAdapter(RequestLoanReactiveRepository repository, ObjectMapper mapper) {
        /**
         * Could be use mapper.mapBuilder if your domain model implement builder pattern
         * super(repository, mapper, d ->
         * mapper.mapBuilder(d,ObjectModel.ObjectModelBuilder.class).build());
         * Or using mapper.map with the class of the object model
         */
        super(repository, mapper, d -> mapper.map(d, RequestLoan.class/* change for domain model */));
    }

}
