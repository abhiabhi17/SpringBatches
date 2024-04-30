package com.example.springbatches.config;

import com.example.springbatches.entity.Customer;
import org.springframework.batch.item.ItemProcessor;


// Reade the input oBject as Customer
// Wtrite the object as customer (Inbound and outbound))
public class CustomerProcessor implements ItemProcessor<Customer,Customer> {


    // this is from qa
    @Override
    public Customer process(Customer customer) throws Exception {
//        if(customer.getCountry().equals("United States")) {
//            return customer;
//        }else{
//            return null;
//        }
        return customer;
    }
}
