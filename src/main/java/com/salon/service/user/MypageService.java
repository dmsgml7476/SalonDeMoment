package com.salon.service.user;


import com.salon.constant.LikeType;
import com.salon.constant.WebTarget;
import com.salon.dto.DayOffShowDto;
import com.salon.dto.shop.CouponListDto;
import com.salon.dto.shop.ReviewImageDto;
import com.salon.dto.user.*;

import com.salon.entity.Review;
import com.salon.entity.ReviewImage;
import com.salon.entity.admin.WebNotification;
import com.salon.entity.management.MemberCoupon;
import com.salon.entity.management.Payment;
import com.salon.entity.management.master.Ticket;

import com.salon.entity.shop.SalonLike;
import com.salon.entity.shop.Shop;
import com.salon.repository.ReviewImageRepo;
import com.salon.repository.ReviewRepo;
import com.salon.repository.WebNotificationRepo;
import com.salon.repository.management.MemberCouponRepo;
import com.salon.repository.management.PaymentRepo;

import com.salon.repository.management.ShopDesignerRepo;
import com.salon.repository.management.master.TicketRepo;
import com.salon.repository.shop.SalonLikeRepo;
import com.salon.repository.shop.ShopImageRepo;
import com.salon.repository.shop.ShopRepo;
import com.salon.service.management.master.CouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.security.access.AccessDeniedException;


@Service
@RequiredArgsConstructor
public class MypageService {
    private final MemberCouponRepo memberCouponRepo;
    private final TicketRepo ticketRepo;
    private final PaymentRepo paymentRepo;
    private final ShopDesignerRepo shopDesignerRepo;
    private final SalonLikeRepo salonLikeRepo;
    private final ShopRepo shopRepo;
    private final CouponService couponService;
    private final ReviewService reviewService;
    private final ShopImageRepo shopImageRepo;
    private final ReviewRepo reviewRepo;
    private final ReviewImageRepo reviewImageRepo;
    private final WebNotificationRepo webNotificationRepo;

    // 마이 쿠폰

    public List<CouponListDto> getMyCoupons(Long memberId) {
        LocalDate today = LocalDate.now();
        List<MemberCoupon> memberCoupons = memberCouponRepo.findAvailableCouponsByMemberId(memberId);

        return memberCoupons.stream()
                .map(mc -> CouponListDto.from(mc.getCoupon(), mc.getCoupon().getShop()))
                .toList();
    }

    public List<MyTicketListDto> getMyTicket(Long memberId) {
        List<Ticket> tickets = ticketRepo.findByMemberId(memberId);
        List<MyTicketListDto> result = new ArrayList<>();

        for (Ticket ticket : tickets) {
            List<Payment> payments = paymentRepo.findByTicketId(ticket.getId());

            if (payments.isEmpty()) {
                // 결제 이력이 없는 정액권도 보여주기
                result.add(MyTicketListDto.from(ticket, null));
            } else {
                for (Payment payment : payments) {
                    result.add(MyTicketListDto.from(ticket, payment));
                }
            }
        }

        return result;
    }

    // 내 찜 목록
    // 내 디자이너

    public List<LikeDesignerDto> getDesignerLike(Long memberId) {
        List<SalonLike> salonLikes = salonLikeRepo.findByMemberIdAndLikeType(memberId, LikeType.DESIGNER);

        List<LikeDesignerDto> result = new ArrayList<>();

        for (SalonLike salonLike : salonLikes) {
            Long designerId = salonLike.getTypeId();

            shopDesignerRepo.findById(designerId).ifPresent(shopDesigner -> {
                LikeDesignerDto dto = LikeDesignerDto.from(salonLike, shopDesigner);
                result.add(dto);
            });
        }

        return result;
    }

    public List<LikeShopDto> getShopLike (Long memberId) {
        List<SalonLike> salonLikes = salonLikeRepo.findByMemberIdAndLikeType(memberId, LikeType.SHOP);
        List<LikeShopDto> result = new ArrayList<>();

        for(SalonLike salonLike : salonLikes) {
            Shop shop = shopRepo.findById(salonLike.getTypeId())
                    .orElse(null);

            if (shop == null) continue;

            boolean hasCoupon = couponService.hasActiveCoupon(shop.getId());

            int reviewCount = reviewService.getReviewCountByShop(shop.getId());
            float avgRating = reviewService.getAverageRatingByShop(shop.getId());

            DayOffShowDto dayOffShowDto = new DayOffShowDto(shop.getDayOff());


            LikeShopDto dto = LikeShopDto.from(salonLike, shop, shopImageRepo, avgRating, reviewCount, hasCoupon, dayOffShowDto);


            result.add(dto);
        }

        return result;
    }

    // 내 리뷰 목록


    //리뷰 목록 불러오기

    public Page<MyReviewListDto> getMyReviewList(Long memberId, Pageable pageable) {

        Page<Review> reviewPage = reviewRepo.findByReservation_Member_Id(memberId, pageable);

        // 각 리뷰 → MyReviewListDto 변환
        List<MyReviewListDto> content = reviewPage.getContent().stream().map(review -> {
            // 이미지 가져오기
            List<ReviewImageDto> reviewImageDtos = reviewImageRepo.findAllByReview_Id(review.getId())
                    .stream()
                    .map(ReviewImageDto::from)
                    .collect(Collectors.toList());

            // 알림 가져오기
            WebNotification webNotification = webNotificationRepo
                    .findTopByWebTargetAndTargetIdAndMemberId(WebTarget.REVIEW, review.getId(), memberId)
                    .orElse(null);

            return MyReviewListDto.from(review, reviewImageDtos, webNotification);

        }).collect(Collectors.toList());

        return new PageImpl<>(content, pageable, reviewPage.getTotalElements());
    }
//
//    public List<MyReviewListDto> getMyReviewList(Long memberId) {
//
//        List<Review> reviews = reviewRepo.findByReservation_Member_id(memberId);
//        List<MyReviewListDto> result = new ArrayList<>();
//
//        for(Review review : reviews) {
//            List<ReviewImage> reviewImages = reviewImageRepo.findAllByReview_Id(review.getId());
//            List<ReviewImageDto> reviewImageDtos = reviewImages.stream()
//                    .map(ReviewImageDto::from)
//                    .collect(Collectors.toList());
//
//
//            // 알림 가져오기
//            WebNotification webNotification = webNotificationRepo
//                    .findTopByWebTargetAndTargetIdAndMemberId(WebTarget.REVIEW, review.getId(), memberId)
//                    .orElse(null);
//
//            // Dto 생성
//            result.add(MyReviewListDto.from(review, reviewImageDtos, webNotification));
//        }
//
//        return result;
//    }

    // 리뷰 상세보기

    public MyReviewDetailDto getMyReviewDetail(Long reviewId, Long memberId) {
        Review review = reviewRepo.findById(reviewId).orElseThrow(() -> new IllegalArgumentException("리뷰가 존재하지 않습니다."));

        // 소유자 검증 코드
        if(!review.getReservation().getMember().getId().equals(memberId)) {
            throw new AccessDeniedException("본인 리뷰만 열람 가능합니다.");
        }

        // 리뷰 이미지

        List<ReviewImageDto> imgDtos = reviewImageRepo.findAllByReview_Id(reviewId)
                .stream()
                .map(ReviewImageDto::from)
                .collect(Collectors.toList());

        WebNotification notify = webNotificationRepo.findTopByWebTargetAndTargetIdAndMemberId(WebTarget.REVIEW, reviewId, memberId)
                .orElse(null);

        if(notify != null && !notify.isRead()) {
            notify.setRead(true);
            webNotificationRepo.save(notify);
        }

        MyReviewListDto listDto = MyReviewListDto.from(review, imgDtos, notify);

        return MyReviewDetailDto.from(review, listDto);

    }

}
