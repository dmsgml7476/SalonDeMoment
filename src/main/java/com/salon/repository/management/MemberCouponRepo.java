package com.salon.repository.management;

import com.salon.entity.management.MemberCoupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface MemberCouponRepo extends JpaRepository<MemberCoupon, Long> {


    @Query("SELECT mc FROM MemberCoupon mc " +
            "WHERE mc.member.id = :memberId " +
            "AND mc.isUsed = false " +
            "AND mc.coupon.isActive = true " +
            "AND mc.coupon.expireDate >= CURRENT_DATE")
    List<MemberCoupon> findAvailableCouponsByMemberId(@Param("memberId") Long memberId);

}
