package org.recipefinder.recipefinder.auth;

import org.recipefinder.recipefinder.customer.Customer;
import org.recipefinder.recipefinder.customer.CustomerRepository;
import org.recipefinder.recipefinder.exceptions.customer.CustomerNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class LoggedInCustomerService {

    private final CustomerRepository customerRepository;

    @Autowired
    public LoggedInCustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public Customer getLoggedInCustomer(Authentication authentication) throws CustomerNotFoundException {
        return customerRepository.findCustomerByEmail(authentication.getName())
                                 .orElseThrow(() -> new CustomerNotFoundException("Customer not found"));
    }
}
