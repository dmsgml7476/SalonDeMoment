package com.salon.repository.admin;

import com.salon.entity.Member;
import com.salon.entity.admin.CsCustomer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CsCustomerRepo extends JpaRepository<CsCustomer, Long> {
    List<CsCustomer> findByMember(Member member);
}
