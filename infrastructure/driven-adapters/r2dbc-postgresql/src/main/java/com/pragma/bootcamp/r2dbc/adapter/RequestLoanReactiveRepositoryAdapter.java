package com.pragma.bootcamp.r2dbc.adapter;

import com.pragma.bootcamp.common.FilterCriteria;
import com.pragma.bootcamp.common.PageRequest;
import com.pragma.bootcamp.common.PageResponse;
import com.pragma.bootcamp.model.requestloan.RequestLoan;
import com.pragma.bootcamp.model.requestloan.RequestLoanInfo;
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

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Repository
public class RequestLoanReactiveRepositoryAdapter extends
        ReactiveAdapterOperations<RequestLoan, RequestLoanEntity, Long, RequestLoanReactiveRepository>
        implements RequestLoanRepository {
    private final RequestLoanEntityMapper requestLoanMapper;
    private final TransactionalGateway transactionalGateway;
    private final R2dbcEntityTemplate template;
    private final Long APPROVED_STATUS = 2L;

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
    public Mono<RequestLoan> findRequestLoanById(Long idRequestLoan) {
        return repository.findById(idRequestLoan).map(requestLoanMapper::toDomain);
    }

    @Override
    public Mono<RequestLoan> update(RequestLoan requestLoanRequestLoanUpdate) {
        return transactionalGateway.doInTransaction(
            repository.findById(requestLoanRequestLoanUpdate.getId())
                .flatMap(existingEntity -> {
                    RequestLoanEntity updatedEntity = requestLoanMapper.toEntity(requestLoanRequestLoanUpdate);
                    updatedEntity.setId(existingEntity.getId());
                    return repository.save(updatedEntity);
                })
                .map(requestLoanMapper::toDomain)
        );
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
                .doOnNext(savedEntity -> log.info("Entity after save: {}", savedEntity))
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

    @Override
    public Mono<PageResponse<RequestLoanInfo>> findWithFiltersInfo(PageRequest pageRequest) {
        return buildQuery(pageRequest.getFilters())
                .flatMap(criteria -> {
                    // Query principal con paginación
                    Query query = Query.query(criteria)
                            .offset((long) pageRequest.getPage() * pageRequest.getSize())
                            .limit(pageRequest.getSize());

                    // Query para contar total
                    Query countQuery = Query.query(criteria);

                    return Mono.zip(
                            template.select(query, RequestLoanEntity.class).collectList(),
                            template.count(countQuery, RequestLoanEntity.class)
                    );
                })
                .flatMap(tuple -> {
                    List<RequestLoanEntity> entities = tuple.getT1();
                    Long totalElements = tuple.getT2();

                    // Obtener DNIs únicos para calcular sumas
                    Set<String> uniqueDnis = entities.stream()
                            .map(RequestLoanEntity::getDni)
                            .collect(Collectors.toSet());

                    // Calcular suma de préstamos aprobados por cada DNI
                    return getApprovedLoansSumByDnis(uniqueDnis)
                            .map(approvedSums -> {
                                List<RequestLoanInfo> content = entities.stream()
                                        .map(entity -> toModelWithApprovedSum(entity, approvedSums))
                                        .toList();

                                return PageResponse.<RequestLoanInfo>builder()
                                        .content(content)
                                        .page(pageRequest.getPage())
                                        .size(pageRequest.getSize())
                                        .totalElements(totalElements)
                                        .totalPages((int) Math.ceil((double) totalElements / pageRequest.getSize()))
                                        .first(pageRequest.getPage() == 0)
                                        .last(pageRequest.getPage() >= (int) Math.ceil((double) totalElements / pageRequest.getSize()) - 1)
                                        .build();
                            });
                });
    }

    @Override
    public Flux<RequestLoan> findApprovedLoansByDni(String dni) {
        return repository.findByDniAndRequestStatus(dni,APPROVED_STATUS)
                .map(requestLoanMapper::toDomain);
    }

    @Override
    public Mono<Long> countAllApprovedLoans() {
        return repository.countByRequestStatus(APPROVED_STATUS);
    }

    @Override
    public Mono<BigDecimal> sumAllApprovedLoans() {
        String sql = """
            SELECT COALESCE(SUM(amount), 0) AS approved_sum 
            FROM requestloan 
            WHERE id_state = :approvedStatus
            """;

        return template.getDatabaseClient()
                .sql(sql)
                .bind("approvedStatus", APPROVED_STATUS)
                .map(row -> row.get("approved_sum", BigDecimal.class))
                .one();
    }

    private Mono<Map<String, BigDecimal>> getApprovedLoansSumByDnis(Set<String> dnis) {
        if (dnis.isEmpty()) {
            return Mono.just(new HashMap<>());
        }

        // Asumiendo que el estado aprobado es 2 (ajustar según tu lógica de negocio)
        String sql = """
                SELECT dni, COALESCE(SUM(amount), 0) as approved_sum 
                FROM requestloan 
                WHERE dni IN (:dnis) AND id_state = :approvedStatus 
                GROUP BY dni
                """;

        return template.getDatabaseClient()
                .sql(sql)
                .bind("dnis", dnis)
                .bind("approvedStatus", APPROVED_STATUS) // Estado aprobado - ajustar según tu caso
                .map(row -> Map.entry(
                        row.get("dni", String.class),
                        row.get("approved_sum", BigDecimal.class)
                ))
                .all()
                .collectMap(Map.Entry::getKey, Map.Entry::getValue)
                .doOnNext(result -> log.debug("Approved loans sums: {}", result));
    }

    private RequestLoanInfo toModelWithApprovedSum(RequestLoanEntity entity, Map<String, BigDecimal> approvedSums) {
        BigDecimal approvedSum = approvedSums.getOrDefault(entity.getDni(), BigDecimal.ZERO);

        return RequestLoanInfo.builder()
                .id(entity.getId())
                .amount(entity.getAmount())
                .email(entity.getEmail())
                .dni(entity.getDni())
                .term(entity.getTerm())
                .requestStatus(entity.getRequestStatus())
                .loanType(entity.getLoanType())
                .approvedLoansSum(approvedSum) // NUEVO CAMPO
                .build();
    }
}
