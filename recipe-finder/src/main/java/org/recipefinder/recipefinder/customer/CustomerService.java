package org.recipefinder.recipefinder.customer;

import org.recipefinder.recipefinder.exceptions.customer.CustomerNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;

    @Autowired
    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    private Customer findCustomer(Long id) throws CustomerNotFoundException {
        return customerRepository.findById(id)
                                 .orElseThrow(() -> new CustomerNotFoundException("Customer with id " + id + " not found"));
    }
}
