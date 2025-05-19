package dev.gavin.customer_service.service;

import dev.gavin.common.dto.AccountDTO;
import dev.gavin.common.dto.CustomerDTO;
import dev.gavin.common.exception.BadRequestException;
import dev.gavin.common.exception.InternalErrorException;
import dev.gavin.common.exception.ResourceNotFoundException;
import dev.gavin.customer_service.dto.CustomerSearchCriteria;
import dev.gavin.customer_service.entity.Customer;
import dev.gavin.customer_service.repository.CustomerRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class CustomerService {
    private static final Logger log = LoggerFactory.getLogger(CustomerService.class);
    private final CustomerRepository customerRepository;

    private final RestTemplate restTemplate;

    @Value("${account-service.url}")
    private String accountServiceUrl;

    public CustomerService(CustomerRepository customerRepository, RestTemplate restTemplate) {
        this.customerRepository = customerRepository;
        this.restTemplate = restTemplate;
    }

    public static Customer toEntity(Customer customer, CustomerDTO customerDTO) {
        if (customer == null)
            customer = new Customer();
        customer.setFirstName(customerDTO.getFirstName());
        customer.setLastName(customerDTO.getLastName());
        customer.setOtherName(customerDTO.getOtherName());
        return customer;
    }

    @Transactional
    public CustomerDTO createCustomer(CustomerDTO customerRequest) {
        validateDTO(customerRequest);

        Customer customer = toEntity(null, customerRequest);
        customer.setCreatedAt(LocalDateTime.now());
        Customer savedCustomer = customerRepository.save(customer);
        return this.fromEntity(savedCustomer);
    }

    @Transactional
    public CustomerDTO getCustomerById(Long id) {
        Customer customer = this.findCustomerById(id);
        CustomerDTO customerDTO = fromEntity(customer);

        try {
            String url = accountServiceUrl + "/api/accounts/customer/" + id;
            AccountDTO[] accountDTOs = restTemplate.getForObject(url, AccountDTO[].class);
            if (accountDTOs != null)
                customerDTO.setAccounts(List.of(accountDTOs));
        } catch (Exception e) {
            log.error("Failed to get accounts for customer: {}", e.getMessage());
        }

        return this.fromEntity(customer);
    }

    @Transactional
    public Page<CustomerDTO> fetchCustomers(CustomerSearchCriteria criteria, Pageable pageable) {
        LocalDateTime startDateTime = criteria.getStartDate() != null ?
                criteria.getStartDate().atStartOfDay() : null;

        LocalDateTime endDateTime = criteria.getEndDate() != null ?
                LocalDateTime.of(criteria.getEndDate(), LocalTime.MAX) : null;

        return customerRepository.fetchCustomers(
                criteria.getName() != null ? criteria.getName().trim().toLowerCase() : null,
                startDateTime,
                endDateTime,
                pageable
        ).map(customer -> {
            CustomerDTO customerDTO = new CustomerDTO();
            customerDTO.setId(customer.getId());
            customerDTO.setFirstName(customer.getFirstName());
            customerDTO.setLastName(customer.getLastName());
            customerDTO.setOtherName(customer.getOtherName());
            customerDTO.setCreatedAt(customer.getCreatedAt());
            customerDTO.setUpdatedAt(customer.getUpdatedAt());
            return customerDTO;
        });
    }

    @Transactional
    public CustomerDTO updateCustomer(Long id, CustomerDTO customerDTO) {
        validateDTO(customerDTO);

        Customer customer = findCustomerById(id);
        customer = toEntity(customer, customerDTO);

        customer.setUpdatedAt(LocalDateTime.now());
        Customer updatedCustomer = customerRepository.save(customer);
        return this.fromEntity(updatedCustomer);
    }

    @Transactional
    public void deleteCustomer(Long id) {
        if (!customerRepository.existsById(id)) {
            throw new ResourceNotFoundException("Customer", "id", String.valueOf(id));
        }

        long accounts;
        try {
            String url = accountServiceUrl + "/api/accounts/count/customer/" + id;
            Long response = restTemplate.getForObject(url, Long.class);
            accounts = response != null ? response : 0L;
        } catch (Exception e) {
            log.error("Error fetching account details: {}", e.getMessage());
            throw new InternalErrorException("Error fetching account details", e);
        }

        if (accounts > 0L) {
            throw new BadRequestException(String.format("Customer has %d account%s and cannot be deleted", accounts, accounts > 1 ? "s" : ""));
        }

        customerRepository.deleteById(id);
    }

    public boolean existsById(Long id) {
        return customerRepository.existsById(id);
    }

    private Customer findCustomerById(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", String.valueOf(id)));
    }

    private void validateDTO(CustomerDTO customerDTO) {
        if (customerDTO.getFirstName() == null || customerDTO.getFirstName().isBlank())
            throw new BadRequestException("First name is required");
        if (customerDTO.getLastName() == null || customerDTO.getLastName().isBlank())
            throw new BadRequestException("Last name is required");

    }

    public CustomerDTO fromEntity(dev.gavin.customer_service.entity.Customer customer) {
        CustomerDTO customerDTO = new CustomerDTO();
        customerDTO.setId(customer.getId());
        customerDTO.setFirstName(customer.getFirstName());
        customerDTO.setLastName(customer.getLastName());
        customerDTO.setOtherName(customer.getOtherName());
        customerDTO.setCreatedAt(customer.getCreatedAt());
        customerDTO.setUpdatedAt(customer.getUpdatedAt());
        return customerDTO;
    }

}
