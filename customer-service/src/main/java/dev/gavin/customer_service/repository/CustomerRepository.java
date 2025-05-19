package dev.gavin.customer_service.repository;

import dev.gavin.customer_service.entity.Customer;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long>, JpaSpecificationExecutor<Customer> {


    default Page<Customer> fetchCustomers(String name, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        return findAll((Specification<Customer>) (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (name != null && !name.isEmpty()) {
                String searchTerm = "%" + name.toLowerCase() + "%";
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("firstName")), searchTerm),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("lastName")), searchTerm),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("otherName")), searchTerm)
                ));
            }

            // Add startDate filter if provided
            if (startDate != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                        root.get("createdAt"),
                        startDate
                ));
            }

            // Add endDate filter if provided
            if (endDate != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                        root.get("createdAt"),
                        endDate
                ));
            }

            // Return the final predicate
            return predicates.isEmpty()
                    ? criteriaBuilder.conjunction()
                    : criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        }, pageable);
    }
}
