package com.salon.repository.management.master;

import com.salon.entity.management.master.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TicketRepo extends JpaRepository<Ticket, Long> {

    // 회원 보유 정액권 목록 가져오기
    List<Ticket> findByMemberIdOrderByCreateAtDesc(Long memberId);

    // 해당 미용실의 정액권 보유목록 가져오기
    Optional<Ticket> findByMemberIdAndShopId(Long memberId, Long shopId);

    List<Ticket> findByMemberId(Long memberId);
}
