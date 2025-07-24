package com.salon.repository;

import com.salon.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.salon.entity.ReviewImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepo extends JpaRepository<Review, Long> {

    // 디자이너 리뷰갯수
    int countByReservation_ShopDesigner_Id(Long designerId);

    // 샵에 달린 전체 리뷰 갯수(소속 디자이너 리뷰갯수 더한거 총 수 )
    @Query("""
            SELECT COUNT(r) FROM Review r JOIN r.reservation res 
            JOIN res.shopDesigner sd WHERE sd.shop.id = :shopId""")
    int countAllByShopId(@Param("shopId") Long shopId);

    // 샵 평균 평점
    @Query("""
            SELECT COALESCE(AVG(r.rating), 0) 
            FROM Review r 
            JOIN r.reservation res 
            JOIN res.shopDesigner sd 
            WHERE sd.shop.id = :shopId
            """)
    float averageRatingByShopId(@Param("shopId") Long shopId);

    // 리뷰 예약 아이디로 찾기
    Optional<Review> findByReservationId(Long id);

    // 해당 유저가 작성한 리뷰 전부 불러오기
    List<Review> findByReservation_ShopDesigner_IdOrderByCreateAtDesc(Long designerId);

    // 페이지 에이블 추가
    Page<Review> findByReservation_Member_Id(Long memberId, Pageable pageable);


    // 디자이너 평점
    @Query("""
    SELECT AVG(r.rating)
    FROM Review r
    WHERE r.reservation.shopDesigner.designer.id = :designerId
    """)
    Float findAvgRatingByDesignerId(@Param("designerId") Long designerId);

    Review findTopByReservation_ShopDesigner_Designer_IdOrderByCreateAtDesc(Long designerId);

    List<Review> findTop10ByReservation_ShopDesigner_Designer_IdOrderByCreateAtDesc(Long designerId);




    // 디자이너 리뷰 목록
    List<Review> findByReservation_shopDesignerId( Long shopDesignerId );

    // 예약 id로 리뷰 리스트 찾기
    List<Review> findByReservationIdIn(List<Long> reservationIds);



}
