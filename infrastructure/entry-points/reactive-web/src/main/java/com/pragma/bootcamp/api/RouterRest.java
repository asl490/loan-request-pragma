            package com.pragma.bootcamp.api;

import com.pragma.bootcamp.api.dto.PageRequestDTO;
import com.pragma.bootcamp.api.dto.RequestLoanCreateDTO;
import com.pragma.bootcamp.api.dto.UpdateStateDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import org.springdoc.core.annotations.RouterOperation;
import org.springdoc.core.annotations.RouterOperations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RequestPredicates.PUT;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class RouterRest {
    private static final String API_V1_PATH = "/api/v1/solicitud";

    @Bean
    @RouterOperations({
            @RouterOperation(
                    path = "/api/request-loan",
                    method = RequestMethod.POST,
                    beanClass = Handler.class,
                    beanMethod = "createRequestLoan",
                    operation = @Operation(
                            operationId = "createRequestLoan",
                            summary = "Crear solicitud de préstamo",
                            description = "Crea una nueva solicitud de préstamo.",
                            requestBody = @RequestBody(
                                    required = true,
                                    content = @Content(
                                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                                            schema = @Schema(implementation = RequestLoanCreateDTO.class)
                                    )
                            ),
                            responses = {
                                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                                            responseCode = "200",
                                            description = "Solicitud de préstamo creada exitosamente"
                                    )
                            }
                    )
            ),
            @RouterOperation(
                    path = API_V1_PATH + "/{id}/estado",
                    method = RequestMethod.PUT,
                    beanClass = Handler.class,
                    beanMethod = "updateRequestState",
                    operation = @Operation(
                            operationId = "updateRequestState",
                            summary = "Aprobar o Rechazar solicitud",
                            description = "Permite a un asesor aprobar o rechazar una solicitud de préstamo.",
                            parameters = {
                                    @Parameter(name = "id", description = "ID de la solicitud de préstamo", required = true, in = ParameterIn.PATH)
                            },
                            requestBody = @RequestBody(
                                    required = true,
                                    content = @Content(
                                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                                            schema = @Schema(implementation = UpdateStateDto.class)
                                    )
                            )
                    )
            ),
            @RouterOperation(
                    path = "/api/v1/request-loans/search",
                    method = RequestMethod.POST,
                    beanClass = Handler.class,
                    beanMethod = "searchWithFilters",
                    operation = @Operation(
                            operationId = "searchRequestLoans",
                            summary = "Buscar solicitudes de préstamo con filtros",
                            description = "Permite buscar solicitudes de préstamo utilizando filtros.",
                            requestBody = @RequestBody(
                                    required = true,
                                    content = @Content(
                                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                                            schema = @Schema(implementation = PageRequestDTO.class)
                                    )
                            ),
                            responses = {
                                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                                            responseCode = "200",
                                            description = "Búsqueda realizada exitosamente"
                                    )
                            }
                    )
            ),
            @RouterOperation(
                    path = "/api/v1/request-loans/search-info",
                    method = RequestMethod.POST,
                    beanClass = Handler.class,
                    beanMethod = "searchWithFilters",
                    operation = @Operation(
                            operationId = "searchRequestLoans",
                            summary = "Buscar solicitudes de préstamo con filtros",
                            description = "Permite buscar solicitudes de préstamo utilizando filtros.",
                            requestBody = @RequestBody(
                                    required = true,
                                    content = @Content(
                                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                                            schema = @Schema(implementation = PageRequestDTO.class)
                                    )
                            )

                    )
            )
    })
    public RouterFunction<ServerResponse> routeRequestLoan(Handler handler) {
        return route(POST(API_V1_PATH), handler::createRequestLoan)
                .andRoute(PUT(API_V1_PATH + "/{id}/estado"), handler::updateRequestState)
                .andRoute(POST("/api/v1/request-loans/search"), handler::searchWithFilters)
                .andRoute(POST("/api/v1/request-loans/search-info"), handler::searchWithFiltersInfo);
    }
}
