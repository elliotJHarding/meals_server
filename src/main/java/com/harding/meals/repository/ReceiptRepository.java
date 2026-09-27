package com.harding.meals.repository;

import com.harding.meals.entity.receipt.Receipt;
import com.harding.meals.entity.user.AppUser;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ReceiptRepository extends CrudRepository<Receipt, Long> {

    @Query("""
            from Receipt receipt where (receipt.user = :user or
                receipt.user.familyGroup in (
                    select u.familyGroup from AppUser u where u = :user
                ))
            order by receipt.orderDate desc
            """)
    List<Receipt> findByFamilyGroup(AppUser user);
}
