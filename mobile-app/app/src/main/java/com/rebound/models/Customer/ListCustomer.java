package com.rebound.models.Customer;

import java.io.Serializable;
import java.util.ArrayList;

public class ListCustomer implements Serializable {
    private ArrayList<Customer> customers;

    public ListCustomer() {
        customers = new ArrayList<>();
    }

    public ArrayList<Customer> getCustomers() {
        return customers;
    }

    public void setCustomers(ArrayList<Customer> customers) {
        this.customers = customers;
    }

    public void addCustomer(Customer c) {
        customers.add(c);
    }

}
