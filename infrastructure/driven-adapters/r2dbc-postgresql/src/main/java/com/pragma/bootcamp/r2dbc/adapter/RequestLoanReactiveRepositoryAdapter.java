package com.pragma.bootcamp.r2dbc.adapter;

import com.pragma.bootcamp.common.FilterCriteria;
import com.pragma.bootcamp.common.PageRequest;
import com.pragma.bootcamp.common.PageResponse;
import com.pragma.bootcamp.model.requestloan.RequestLoan;
import com.pragma.bootcamp.model.requestloan.gateways.RequestLoanRepository;
import com.pragma.bootcamp.r2dbc.entity.RequestLoanEntity;
import com.pragma.bootcamp.r2dbc.helper.ReactiveAdapterOperations;
import com.pragma.bootcamp.r2dbc.mapper.RequestLoanEntityMapper;
import com.pragma.bootcamp.utils.gateways.TransactionalGateway;
import lombok.extern.slf4j.Slf4j;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@Repository
public class RequestLoanReactiveRepositoryAdapter extends
        ReactiveAdapterOperations<RequestLoan, RequestLoanEntity, Long, RequestLoanReactiveRepository>
        implements RequestLoanRepository {
    private final RequestLoanEntityMapper requestLoanMapper;
    private final TransactionalGateway transactionalGateway;
    private final R2dbcEntityTemplate template;

    public RequestLoanReactiveRepositoryAdapter(RequestLoanReactiveRepository repository, ObjectMapper mapper,
                                                RequestLoanEntityMapper requestLoanMapper, TransactionalGateway transactionalGateway, R2dbcEntityTemplate template) {
        super(repository, mapper, d -> mapper.map(d, RequestLoan.class));
        this.requestLoanMapper = requestLoanMapper;
        this.transactionalGateway = transactionalGateway;
        this.template = template;
    }

    @Override
    public Flux<RequestLoan> getAll() {
        return repository.findAll().map(requestLoanMapper::toDomain);
    }

    @Override
    public Mono<Void> delete(Long idRequestLoan) {
        return repository.deleteById(idRequestLoan);
    }

    @Override
    public Mono<RequestLoan> createLoan(RequestLoan requestLoan) {
        log.trace("Saving new RequestLoan with document: {}", requestLoan.toString());
        RequestLoanEntity requestLoanEntity = requestLoanMapper.toEntity(requestLoan);

        return transactionalGateway.doInTransaction(repository.save(requestLoanEntity)
                .doOnNext(savedEntity -> log.info("Entity after save: {}", savedEntity)) // verifica el ID aquí
                .map(requestLoanMapper::toDomain)
                .doOnSuccess(savedRequestLoan -> log.info("Successfully created RequestLoan with ID: {}",
                        savedRequestLoan.getId())));
    }

    @Override
    public Mono<PageResponse<RequestLoan>> findWithFilters(PageRequest pageRequest) {
        return buildQuery(pageRequest.getFilters())
                .flatMap(criteria -> {
                    Query query = Query.query(criteria)
                            .offset((long) pageRequest.getPage() * pageRequest.getSize())
                            .limit(pageRequest.getSize());

//                    if (pageRequest.getSortBy() != null) {
//                        query = "DESC".equalsIgnoreCase(pageRequest.getSortDirection()) ?
//                                query.sort(org.springframework.data.domain.Sort.by(pageRequest.getSortBy()).descending()) :
//                                query.sort(org.springframework.data.domain.Sort.by(pageRequest.getSortBy()).ascending());
//                    }

                    Query countQuery = Query.query(criteria);

                    return Mono.zip(
                            template.select(query, RequestLoanEntity.class).collectList(),
                            template.count(countQuery, RequestLoanEntity.class)
                    );
                })
                .map(tuple -> {
                    List<RequestLoanEntity> entities = tuple.getT1();
                    Long totalElements = tuple.getT2();

                    List<RequestLoan> content = entities.stream()
                            .map(requestLoanMapper::toDomain)
                            .toList();

                    return PageResponse.<RequestLoan>builder()
                            .content(content)
                            .page(pageRequest.getPage())
                            .size(pageRequest.getSize())
                            .totalElements(totalElements)
                            .totalPages((int) Math.ceil((double) totalElements / pageRequest.getSize()))
                            .first(pageRequest.getPage() == 0)
                            .last(pageRequest.getPage() >= (int) Math.ceil((double) totalElements / pageRequest.getSize()) - 1)
                            .build();
                });
    }

    private Mono<Criteria> buildQuery(List<FilterCriteria> filters) {
        if (filters == null || filters.isEmpty()) {
            return Mono.just(Criteria.empty());
        }

        return Mono.fromCallable(() -> {
            Criteria criteria = null;

            for (FilterCriteria filter : filters) {
                Criteria currentCriteria = buildCriteriaFromFilter(filter);

                if (criteria == null) {
                    criteria = currentCriteria;
                } else {
                    criteria = "OR".equalsIgnoreCase(filter.getLogicalOperator()) ?
                            criteria.or(currentCriteria) :
                            criteria.and(currentCriteria);
                }
            }

            return criteria != null ? criteria : Criteria.empty();
        });
    }

    private Criteria buildCriteriaFromFilter(FilterCriteria filter) {
        String field = filter.getField();
        String operator = filter.getOperator();
        Object value = filter.getValue();

        return switch (operator.toLowerCase()) {
            case "eq" -> Criteria.where(field).is(value);
            case "ne" -> Criteria.where(field).not(value);
            case "gt" -> Criteria.where(field).greaterThan(value);
            case "lt" -> Criteria.where(field).lessThan(value);
            case "gte" -> Criteria.where(field).greaterThanOrEquals(value);
            case "lte" -> Criteria.where(field).lessThanOrEquals(value);
            case "like" -> Criteria.where(field).like("%" + value + "%");
            case "in" -> {
                if (value instanceof List<?> list) {
                    yield Criteria.where(field).in(list);
                }
                yield Criteria.where(field).is(value);
            }
            case "isnull" -> Criteria.where(field).isNull();
            case "isnotnull" -> Criteria.where(field).isNotNull();
            default -> Criteria.where(field).is(value);
        };
    }

}
