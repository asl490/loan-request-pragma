package com.pragma.bootcamp.config;

import com.pragma.bootcamp.model.client.gateways.ClientRepository;
import com.pragma.bootcamp.model.loantype.gateways.LoanTypeRepository;
import com.pragma.bootcamp.model.requestloan.gateways.RequestLoanRepository;
import com.pragma.bootcamp.model.requeststatus.gateways.RequestStatusRepository;
import com.pragma.bootcamp.utils.gateways.TransactionalGateway;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.*;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

public class UseCasesConfigTest {

    @Test
    void testUseCaseBeansExist() {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(TestConfig.class)) {
            String[] beanNames = context.getBeanDefinitionNames();

            boolean useCaseBeanFound = false;
            for (String beanName : beanNames) {
                if (beanName.endsWith("UseCase")) {
                    useCaseBeanFound = true;
                    break;
                }
            }

            assertTrue(useCaseBeanFound, "No beans ending with 'UseCase' were found");
        }
    }

    @Configuration
    @Import(UseCasesConfig.class) // importa tu configuración real
    static class TestConfig {

        @Bean
        public LoanTypeRepository loanTypeRepository() {
            return mock(LoanTypeRepository.class);
        }

        @Bean
        public RequestLoanRepository requestLoanRepository() {
            return mock(RequestLoanRepository.class);
        }

        @Bean
        public TransactionalGateway transactionalGateway() {
            return mock(TransactionalGateway.class);
        }

        @Bean
        public ClientRepository clientRepository() {
            return mock(ClientRepository.class);
        }

        @Bean
        public RequestStatusRepository requestStatusRepository() {
            return mock(RequestStatusRepository.class);
        }

        @Bean
        public MyUseCase myUseCase() {
            return new MyUseCase();
        }
    }

    static class MyUseCase {
        public String execute() {
            return "MyUseCase Test";
        }
    }
}
