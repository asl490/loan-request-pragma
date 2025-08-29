package com.pragma.bootcamp.api;

import com.pragma.bootcamp.api.dto.RequestLoanCreateDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import org.springdoc.core.annotations.RouterOperation;
import org.springdoc.core.annotations.RouterOperations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class RouterRest {
    private static final String BASE_PATH = "/api/request-loan";

    @Bean
    @RouterOperations({
            @RouterOperation(path = "/api/request-loan", method = {
                    RequestMethod.POST}, beanClass = Handler.class, beanMethod = "createRequestLoan", operation = @Operation(operationId = "createRequestLoan", summary = "Crear solicitud de préstamo", requestBody = @RequestBody(required = true, content = @Content(schema = @Schema(implementation = RequestLoanCreateDTO.class)))))
    })
    public RouterFunction<ServerResponse> routeRequestLoan(Handler handler) {
        return route(POST(BASE_PATH), handler::createRequestLoan);
    }
}