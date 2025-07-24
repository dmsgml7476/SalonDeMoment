package com.salon.repository.shop;

import com.salon.entity.shop.Shop;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShopRepo extends JpaRepository<Shop, Long> {

    // 매장 이름으로 단일 매장 조회
    Optional<Shop> findByName(String name);

    // 지역으로 매장 검색 (따로 지역이 없으니 주소에서 검색하기)
    Page<Shop> findByAddressContaining(String region, Pageable pageable);

    List<Shop> findByAddressContaining(String region);

    List<Shop> findByNameContainingIgnoreCase(String keyword);


    // 리뷰

    @Query(value = """
    SELECT 
        s.shop_id AS id,
        COALESCE(AVG(r.rating), 0) AS avgRating,
        COUNT(r.review_id) AS reviewCount
    FROM shop s
    LEFT JOIN shop_designer sd ON sd.shop_id = s.shop_id
    LEFT JOIN reservation res ON res.shop_designer_id = sd.shop_designer_id
    LEFT JOIN review r ON r.reservation_id = res.reservation_id
    WHERE s.address LIKE %:region%
    GROUP BY s.shop_id
    ORDER BY reviewCount DESC
    """,
            countQuery = """
    SELECT COUNT(DISTINCT s.shop_id)
    FROM shop s
    WHERE s.address LIKE %:region%
    """,
            nativeQuery = true)
    Page<ShopListProjection> findReviewRatingStatsByRegion(
            @Param("region") String region,
            Pageable pageable
    );

    // 평점

    @Query(value = """
    SELECT 
        s.shop_id AS id,
        COALESCE(AVG(r.rating), 0) AS avgRating,
        COUNT(r.review_id) AS reviewCount
    FROM shop s
    LEFT JOIN shop_designer sd ON sd.shop_id = s.shop_id
    LEFT JOIN reservation res ON res.shop_designer_id = sd.shop_designer_id
    LEFT JOIN review r ON r.reservation_id = res.reservation_id
    WHERE s.address LIKE %:region%
    GROUP BY s.shop_id
    ORDER BY avgRating DESC
    """,
            countQuery = """
    SELECT COUNT(DISTINCT s.shop_id)
    FROM shop s
    WHERE s.address LIKE %:region%
    """,
            nativeQuery = true)
    Page<ShopListProjection> findReviewRatingStatsByRegionOrderByRatingDesc(
            @Param("region") String region,
            Pageable pageable
    );



    // 평점
//    @Query(value = """
//    SELECT s.*, COALESCE(AVG(r.rating), 0) AS avg_rating
//    FROM shop s
//    LEFT JOIN shop_designer sd ON sd.shop_id = s.shop_id
//    LEFT JOIN reservation res ON res.shop_designer_id = sd.shop_designer_id
//    LEFT JOIN review r ON r.reservation_id = res.reservation_id
//    WHERE s.address LIKE %:region%
//    GROUP BY s.shop_id
//    ORDER BY avg_rating DESC
//    """,
//            countQuery = """
//    SELECT COUNT(DISTINCT s.shop_id)
//    FROM shop s
//    WHERE s.address LIKE %:region%
//    """,
//            nativeQuery = true)
//    Page<Shop> findByRegionOrderByAvgRatingDesc(@Param("region") String region, Pageable pageable);

    //라이크
    @Query(value = """
    SELECT s.*, COUNT(sl.id) AS like_count
    FROM shop s
    LEFT JOIN salon_like sl ON sl.type_id = s.shop_id AND sl.like_type = 'SHOP'
    WHERE s.address LIKE %:region%
    GROUP BY s.shop_id
    ORDER BY like_count DESC
    """,
            countQuery = """
    SELECT COUNT(*)
    FROM shop s
    WHERE s.address LIKE %:region%
    """,
            nativeQuery = true)
    Page<Shop> findByRegionOrderByLikeCountDesc(@Param("region") String region, Pageable pageable);



}