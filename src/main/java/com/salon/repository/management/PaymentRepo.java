package com.salon.repository.management;

import com.salon.entity.Member;
import com.salon.entity.management.Payment;
import com.salon.entity.shop.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepo extends JpaRepository<Payment, Long> {

    // 디자이너 매출 목록
//    @Query("""
//    SELECT p FROM Payment p WHERE (p.reservation.shopDesigner.id = :designerId) OR (p.shopDesigner.id = :designerId)
//    """)
//    List<Payment> findByDesignerOrderByPayDate(@Param("designerId") Long designerId);

    // 당일 결제 회원 수 (디자이너)
    @Query("SELECT COUNT(p) FROM Payment p LEFT JOIN p.reservation r " +
    "WHERE ((r.shopDesigner.id = :designerId OR p.shopDesigner.id = :designerId) " +
        "AND DATE(p.payDate) = CURRENT_DATE)")
    int countTodayCompletePayments(@Param("designerId") Long designerId);

    // 결제 내역 가져오기 (기간)
    @Query("SELECT p FROM Payment p LEFT JOIN p.reservation r " +
        "WHERE (p.shopDesigner.id = :designerId OR r.shopDesigner.id = :designerId) " +
        "AND p.payDate BETWEEN :start AND :end ORDER BY p.payDate ASC"
        )
    List<Payment> findByDesignerAndPeriod(@Param("designerId") Long designerId,
                                          @Param("start") LocalDateTime start,
                                          @Param("end") LocalDateTime end);


    // 디자이너의 하루 매출
    @Query("SELECT COALESCE(SUM(p.totalPrice), 0) " +
            "FROM Payment p WHERE DATE(p.payDate) = CURRENT_DATE " +
            "AND p.shopDesigner.id = :designerId")
    int sumTodayTotalPrice(@Param("designerId") Long designerId);

    // 매장 월간 매출
    @Query("SELECT COALESCE(SUM(p.totalPrice), 0) " +
            "FROM Payment p JOIN p.shopDesigner sd JOIN sd.shop s " +
            "WHERE FUNCTION('MONTH', p.payDate) = FUNCTION('MONTH', CURRENT_DATE) " +
            "AND FUNCTION('YEAR', p.payDate) = FUNCTION('YEAR', CURRENT_DATE) " +
            "AND s.id = :shopId")
    int sumMonthlyTotalPrice(@Param("shopId") Long shopId);

    // 예약 아이디 통해 결제 정보 가져오기

    Optional<Payment> findByReservationId(Long reservationId);

    // 페이먼트에서 reservationId 를 통해 ticketid 찾아서 해당 티켓에 연결된 페이먼트만 가져오기
    @Query("""
    SELECT p FROM Payment p
    JOIN FETCH p.reservation r
    JOIN FETCH p.shopDesigner sd
    JOIN FETCH sd.shop s
    WHERE r.ticket.id = :ticketId
    """)
    List<Payment> findByTicketId(@Param("ticketId") Long ticketId);

    // 디자이너에게 시술받은 회원 목록
    @Query("SELECT DISTINCT p.reservation.member FROM Payment p WHERE p.shopDesigner.id = :shopDesignerId")
    List<Member> findDistinctMembersByShopDesignerId(@Param("shopDesignerId") Long shopDesignerId);

    // 디자이너의 해당 회원 결제내역
    @Query("SELECT p FROM Payment p JOIN FETCH p.reservation r " +
            "JOIN FETCH r.member m JOIN FETCH p.shopDesigner d " +
            "WHERE m.id = :memberId AND d.id = :designerId AND p.reservation IS NOT NULL " +
            "ORDER BY p.payDate DESC")
    List<Payment> findByMemberIdAndDesignerId( @Param("memberId") Long memberId, @Param("designerId") Long designerId);

    // 월간 매출 내역
    List<Payment> findByShopDesigner_Shop_IdAndPayDateBetween(Long shopId, LocalDateTime startOfMonth, LocalDateTime endOfMonth);

    // 결제 여부
    boolean existsByReservation(Reservation reservation);
}
