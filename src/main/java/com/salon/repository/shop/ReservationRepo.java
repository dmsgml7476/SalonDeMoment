package com.salon.repository.shop;

import com.salon.entity.management.ShopDesigner;
import com.salon.entity.shop.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ReservationRepo extends JpaRepository<Reservation, Long> {

    // 특정 디자이너의 특정 기간내 예약 리스트 조회
    List<Reservation> findByShopDesignerIdAndReservationDateBetween(Long designerId, LocalDateTime start, LocalDateTime end);

    // 사용자의 전체 예약 조회
    List<Reservation> findByMemberIdOrderByReservationDateDesc(Long memberId);

    // 방문횟수를 카운트하기 위한 메서드
    @Query("""
            SELECT COUNT(r)
            FROM Reservation r
            WHERE r.member.id = :memberId
            AND r.shopDesigner.shop.id = :shopId 
            AND r.status = 'COMPLETED'
            """)
    int countVisitByMemberAndShop(@Param("memberId") Long memberId, @Param("shopId") Long shopId);

    // 오늘 예약 수 (디자이너)
    @Query("SELECT COUNT(r) FROM Reservation r " +
            "WHERE DATE(r.reservationDate) = CURRENT_DATE " +
            "AND r.shopDesigner.id = :designerId")
    int countTodayReservations(@Param("designerId") Long designerId);

    // 당일 신규 예약 회원
    @Query(value = """
    SELECT COUNT(DISTINCT r.member_id)
    FROM reservation r
    WHERE r.shop_designer_id = :designerId
      AND DATE(r.reservation_date) = CURRENT_DATE
      AND r.reservation_date = (
        SELECT MIN(r2.reservation_date)
        FROM reservation r2
        WHERE r2.member_id = r.member_id
      )
    """, nativeQuery = true)
    int countTodayNewCustomers(@Param("designerId") Long designerId);

    // 디자이너 당일 예약 목록
    @Query("SELECT r FROM Reservation r JOIN r.shopDesigner sd " +
            "WHERE DATE(r.reservationDate) = CURRENT_DATE " +
            "AND sd.id = :designerId ORDER BY r.reservationDate")
    List<Reservation> findTodayReservations(@Param("designerId") Long designerId);

    List<Reservation> findByShopDesignerIdAndReservationDateBetweenOrderByReservationDateDesc(Long id, LocalDateTime start, LocalDateTime end);

    // 미용실 당일 예약 목록
    @Query("SELECT r FROM Reservation r JOIN r.shopDesigner sd " +
            "WHERE sd.shop.id = :shopId AND DATE(r.reservationDate) = CURRENT_DATE " +
            "ORDER BY r.reservationDate")
    List<Reservation> findTodayResByShopId(@Param("shopId") Long shopId);

    List<Reservation> findAllByShopDesignerIdIn(List<Long> shopDesignerIds);

    Optional<Reservation> findByIdAndMemberId(Long reservationId, Long memberId);

}
