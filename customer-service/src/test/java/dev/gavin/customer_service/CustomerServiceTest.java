package dev.gavin.customer_service;

import dev.gavin.common.dto.AccountDTO;
import dev.gavin.common.dto.CustomerDTO;
import dev.gavin.common.exception.BadRequestException;
import dev.gavin.common.exception.ResourceNotFoundException;
import dev.gavin.customer_service.dto.CustomerSearchCriteria;
import dev.gavin.customer_service.entity.Customer;
import dev.gavin.customer_service.repository.CustomerRepository;
import dev.gavin.customer_service.service.CustomerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CustomerServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerService customerService;

    private Customer customer;
    private CustomerDTO customerDTO;

    @BeforeEach
    void setUp() {
        customer = new Customer();
        customer.setId(1L);
        customer.setFirstName("John");
        customer.setLastName("Doe");

        customerDTO = new CustomerDTO();
        customerDTO.setId(1L);
        customerDTO.setFirstName("John");
        customerDTO.setLastName("Doe");

        ReflectionTestUtils.setField(customerService, "accountServiceUrl", "http://account-service");
    }

    @Test
    void createCustomer_Success() {
        // Given
        when(customerRepository.save(any(Customer.class))).thenReturn(customer);

        // When
        CustomerDTO result = customerService.createCustomer(customerDTO);

        // Then
        assertNotNull(result);
        assertEquals(customerDTO.getFirstName(), result.getFirstName());
        assertEquals(customerDTO.getLastName(), result.getLastName());
        verify(customerRepository, times(1)).save(any(Customer.class));
    }

    @Test
    void getCustomerById_Success() {
        // Given
        when(customerRepository.findById(anyLong())).thenReturn(Optional.of(customer));
        when(restTemplate.getForObject(anyString(), eq(AccountDTO[].class))).thenReturn(new AccountDTO[0]);

        // When
        CustomerDTO result = customerService.getCustomerById(1L);

        // Then
        assertNotNull(result);
        assertEquals(customerDTO.getId(), result.getId());
        assertEquals(customerDTO.getFirstName(), result.getFirstName());
        verify(customerRepository, times(1)).findById(1L);
        verify(restTemplate, times(1)).getForObject(anyString(), eq(AccountDTO[].class));
    }

    @Test
    void getCustomerById_NotFound_ThrowsException() {
        // Given
        when(customerRepository.findById(anyLong())).thenReturn(Optional.empty());

        // When
        assertThrows(ResourceNotFoundException.class, () -> customerService.getCustomerById(1L));
        verify(customerRepository, times(1)).findById(1L);

        // Then
        verify(restTemplate, never()).getForObject(anyString(), any());
    }

    @Test
    void fetchCustomers_ReturnsPagedCustomerDTOs() {
        // Given
        CustomerSearchCriteria criteria = new CustomerSearchCriteria();
        criteria.setName("John");
        criteria.setStartDate(LocalDateTime.now().toLocalDate());
        criteria.setEndDate(LocalDateTime.now().toLocalDate());

        Pageable pageable = mock(Pageable.class);

        Customer customer1 = new Customer();
        customer1.setId(1L);
        customer1.setFirstName("John");
        customer1.setLastName("Doe");

        Page customerPage = mock(Page.class);
        when(customerRepository.fetchCustomers(
                eq("john"),
                any(LocalDateTime.class),
                any(LocalDateTime.class),
                eq(pageable)
        )).thenReturn(customerPage);

        when(customerPage.map(any())).thenAnswer(invocation -> {
            java.util.function.Function<Customer, CustomerDTO> mapper = invocation.getArgument(0);
            return new org.springframework.data.domain.PageImpl<>(
                    List.of(mapper.apply(customer1))
            );
        });

        // When
        Page<CustomerDTO> result = customerService.fetchCustomers(criteria, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        CustomerDTO dto = result.getContent().get(0);
        assertEquals(customer1.getId(), dto.getId());
        assertEquals(customer1.getFirstName(), dto.getFirstName());
        assertEquals(customer1.getLastName(), dto.getLastName());
        verify(customerRepository, times(1)).fetchCustomers(
                eq("john"),
                any(LocalDateTime.class),
                any(LocalDateTime.class),
                eq(pageable)
        );
    }

    @Test
    void deleteCustomer_Success() {
        // Given
        when(customerRepository.existsById(anyLong())).thenReturn(true);
        when(restTemplate.getForObject(anyString(), eq(Long.class))).thenReturn(0L);

        doNothing().when(customerRepository).deleteById(anyLong());

        // When
        customerService.deleteCustomer(1L);

        // Then
        verify(customerRepository, times(1)).existsById(1L);
        verify(restTemplate, times(1)).getForObject(anyString(), eq(Long.class));
        verify(customerRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteCustomer_NotFound_ThrowsException() {
        // Given
        when(customerRepository.existsById(anyLong())).thenReturn(false);

        // When
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> customerService.deleteCustomer(1L));

        // Then
        assertEquals(String.format("Customer with id, %d cannot be found", 1L), exception.getMessage());
        verify(customerRepository, times(1)).existsById(1L);
        verify(restTemplate, never()).getForObject(anyString(), any());
        verify(customerRepository, never()).deleteById(anyLong());
    }

    @Test
    void deleteCustomer_WithAccounts_ThrowsException() {
        // Given
        when(customerRepository.existsById(anyLong())).thenReturn(true);
        when(restTemplate.getForObject(anyString(), eq(Long.class))).thenReturn(2L);

        // When
        assertThrows(BadRequestException.class, () -> customerService.deleteCustomer(1L));

        //Then
        verify(customerRepository, times(1)).existsById(1L);
        verify(restTemplate, times(1)).getForObject(anyString(), eq(Long.class));
        verify(customerRepository, never()).deleteById(anyLong());
    }
}