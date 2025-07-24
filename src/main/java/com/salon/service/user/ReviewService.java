package com.salon.service.user;


import com.salon.constant.UploadType;
import com.salon.dto.UploadedFileDto;
import com.salon.dto.designer.ReviewReplyDto;
import com.salon.dto.shop.ReviewImageDto;
import com.salon.dto.shop.ReviewListDto;
import com.salon.dto.user.ReviewCreateDto;

import com.salon.entity.Review;
import com.salon.entity.ReviewImage;
import com.salon.entity.management.ShopDesigner;
import com.salon.entity.shop.Reservation;
import com.salon.repository.ReviewImageRepo;
import com.salon.repository.ReviewRepo;
import com.salon.repository.management.ShopDesignerRepo;
import com.salon.repository.shop.ReservationRepo;
import com.salon.util.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepo reviewRepo;
    private final ReservationRepo reservationRepo;
    private final FileService fileService;
    private final ReviewImageRepo reviewImageRepo;
    private final ShopDesignerRepo shopDesignerRepo;

    // 샵 리뷰수
    public int getReviewCountByShop(Long shopId) {
        return reviewRepo.countAllByShopId(shopId);
    }

    // 샵 평균
    public float getAverageRatingByShop(Long shopId) {
        float avg = reviewRepo.averageRatingByShopId(shopId);
        return Math.round(avg * 10) / 10f;
    }

    // 이미지가 있는 최신 리뷰 1건 조회(디자이너기준)
    public Optional<Review> getRecentReviewWithImageByDesigner(Long designerId) {
        List<Review> recent =
                reviewRepo.findTop10ByReservation_ShopDesigner_Designer_IdOrderByCreateAtDesc(designerId);

        return recent.stream()
                .filter(r -> reviewImageRepo.existsByReview_Id(r.getId()))
                .findFirst();
    }

    // 조회한 리뷰 보내주기

    public Optional<ReviewListDto> getRecentReviewDtoWithImageByDesigner(Long designerId) {

        Optional<Review> optReview = getRecentReviewWithImageByDesigner(designerId);
        if (optReview.isEmpty()) return Optional.empty();

        Review review = optReview.get();

        // 디자이너/샵 정보
        ShopDesigner shopDesigner =
                shopDesignerRepo.findByDesigner_Member_IdAndIsActiveTrue(designerId);
        if (shopDesigner == null) return Optional.empty();

        // 리뷰 이미지 DTO (전부 or 첫 장만 필요시 limit(1))
        List<ReviewImageDto> imageDtos = getReviewImages(review.getId());

        Reservation res       = review.getReservation();
        Long memberId         = res.getMember().getId();
        Long shopId           = shopDesigner.getShop().getId();
        int  visitCount       = reservationRepo.countVisitByMemberAndShop(memberId, shopId);
        ReviewReplyDto reply  =
                (review.getReplyComment() != null && review.getReplyAt() != null)
                        ? ReviewReplyDto.from(review)
                        : null;

        ReviewListDto dto = ReviewListDto.from(review, shopDesigner, imageDtos, visitCount, reply);
        dto.setMemberName(res.getMember().getName());

        return Optional.of(dto);
    }



    // 리뷰 저장 서비스
    public void saveReview(ReviewCreateDto dto) {
        Reservation reservation = reservationRepo.findById(dto.getReservationId())
                .orElseThrow(() -> new IllegalArgumentException("예약을 찾을 수 없습니다."));

        Review review = dto.to(reservation);
        reviewRepo.save(review);


        if (dto.getReviewImages() != null) {
            for(MultipartFile multipartFile : dto.getReviewImages()) {
                if(multipartFile != null && !multipartFile.isEmpty()) {
                    UploadedFileDto uploadedFile = fileService.upload(multipartFile, UploadType.REVIEW);

                    ReviewImage image = new ReviewImage();
                    image.setReview(review);
                    image.setOriginalName(uploadedFile.getOriginalFileName());
                    image.setImgName(uploadedFile.getFileName());
                    image.setImgUrl(uploadedFile.getFileUrl());

                    reviewImageRepo.save(image);
                }
            }
        }

    }

    //리뷰 첫번째 이미지 가져오기
    public Optional<ReviewImage> getFirstImageByReviewId(Long reviewId) {
        return Optional.ofNullable(reviewImageRepo.findTopByReview_IdOrderByIdAsc(reviewId));
    }

    // 디자이너 리뷰 목록 조회
    public List<ReviewListDto> getReviewsByDesigner(Long shopDesignerId ){
        ShopDesigner shopDesigner = shopDesignerRepo.findByDesigner_Member_IdAndIsActiveTrue(shopDesignerId);
        if (shopDesignerId == null) {
            throw new IllegalArgumentException("해당 디자이너 정보를 찾을 수 없습니다");
        }

        List<Review> reviews = reviewRepo.findByReservation_shopDesignerId(shopDesignerId);
        List<ReviewListDto> reviewList = new ArrayList<>();

        for (Review review : reviews) {
            Reservation res = review.getReservation();
            Long memberId = res.getMember().getId();
            Long shopId = shopDesigner.getShop().getId();

            // 리뷰이미지 조회
            List<ReviewImageDto> imageDtos = getReviewImages(review.getId());

            // 사용자 방문 횟수 계산
            int visitCount = reservationRepo.countVisitByMemberAndShop(memberId,shopId);

            // 디자이너 답글 조회
            ReviewReplyDto replyDto = null;
            if (review.getReplyComment() != null && review.getReplyAt() != null) {
                replyDto = ReviewReplyDto.from(review);
            }

            // dto 생성
            ReviewListDto dto = ReviewListDto.from(review,shopDesigner,imageDtos, visitCount,replyDto);
            dto.setMemberName(res.getMember().getName());

            reviewList.add(dto);
        }

        return reviewList;
    }

    // 리뷰 이미지 조회
    private List<ReviewImageDto> getReviewImages(Long reviewId) {
        List<ReviewImage> imageList = reviewImageRepo.findByReviewId(reviewId);
        return imageList.stream()
                .filter(Objects::nonNull)
                .map(ReviewImageDto::from)
                .collect(Collectors.toList());
    }

}
