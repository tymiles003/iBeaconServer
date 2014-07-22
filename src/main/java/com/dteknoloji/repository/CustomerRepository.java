package com.dteknoloji.repository;

import java.util.List;
import org.springframework.data.repository.CrudRepository;
import com.dteknoloji.domain.Customer;

public interface CustomerRepository extends CrudRepository<Customer, Long> {
    List<Customer> findUsersByUsernameAndPassword(String u, String p);

    //  @Query("SELECT a FROM Customer c INNER JOIN c.applicationList a WHERE c.identity = :customerId AND a.identity= :appId")
    //  Application findApplicationById(@Param("customerId") Long customerId, @Param("appId") Long appId);
}
