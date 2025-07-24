package com.salon.repository;

import com.salon.dto.shop.ReviewImageDto;
import com.salon.entity.ReviewImage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewImageRepo extends JpaRepository<ReviewImage, Long> {

    // 특정 리뷰에 연결된 모든 이미지 가져오기
    List<ReviewImage> findAllByReview_Id(Long reviewId);

    boolean existsByReview_Id(Long id);

    ReviewImage findTopByReview_IdOrderByIdAsc(Long reviewId);
    // 해당 리뷰의 id를 찾아 이미지 조회하는 메서드
    List<ReviewImage> findByReviewId(Long reviewId);

    // 리뷰 이미지 최신순으로 8장만 가져오기
    List<ReviewImage> findTop8ByOrderByIdDesc();

    // 미용실 페이지 리뷰 썸네일 8개
    @Query("SELECT ri.imgUrl " +
            "FROM ReviewImage ri " +
            "JOIN ri.review r " +
            "JOIN r.reservation res " +
            "JOIN res.shopDesigner sd " +
            "JOIN sd.shop s " +
            "WHERE s.id = :shopId " +
            "ORDER BY r.createAt DESC, ri.id ASC")
    List<String> findTop8ThumbUrl(@Param("shopId") Long shopId, Pageable pageable);
}
