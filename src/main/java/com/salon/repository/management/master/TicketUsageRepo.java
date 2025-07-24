package com.salon.repository.management.master;

import com.salon.entity.management.master.Ticket;
import com.salon.entity.management.master.TicketUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TicketUsageRepo extends JpaRepository<TicketUsage, Long> {


    int findTotalUsedAmountByTicketId(Long id);
}
