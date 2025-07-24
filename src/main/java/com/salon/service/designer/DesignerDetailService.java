package com.salon.service.designer;

import com.salon.constant.LikeType;
import com.salon.constant.ServiceCategory;
import com.salon.dto.designer.DesignerDetailDto;
import com.salon.dto.designer.DesignerHomeDto;
import com.salon.dto.designer.ReviewReplyDto;
import com.salon.dto.management.ServiceForm;
import com.salon.dto.shop.ReviewImageDto;
import com.salon.dto.shop.ReviewListDto;
import com.salon.entity.Review;
import com.salon.entity.ReviewImage;
import com.salon.entity.management.ShopDesigner;
import com.salon.entity.management.master.DesignerService;
import com.salon.entity.management.master.ShopService;
import com.salon.entity.shop.Reservation;
import com.salon.repository.ReviewImageRepo;
import com.salon.repository.ReviewRepo;
import com.salon.repository.management.ShopDesignerRepo;
import com.salon.repository.management.master.DesignerServiceRepo;
import com.salon.repository.management.master.ShopServiceRepo;
import com.salon.repository.shop.ReservationRepo;
import com.salon.repository.shop.SalonLikeRepo;
import com.salon.service.shop.SalonService;
import com.salon.service.user.ReviewService;
import lombok.RequiredArgsConstructor;
import org.hibernate.validator.internal.util.privilegedactions.LoadClass;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DesignerDetailService {


    private final ShopDesignerRepo shopDesignerRepo;
    private final ReviewService reviewService;
    private final SalonLikeRepo salonLikeRepo;
    private final ReviewRepo reviewRepo;
    private final ShopServiceRepo shopServiceRepo;
    private final ReviewImageRepo reviewImageRepo;
    private final ReservationRepo reservationRepo;
    private final DesignerServiceRepo designerServiceRepo;


    // 디자이너 상세정보 조회
    public DesignerDetailDto getDesignerDetail(Long shopDesignerId){
        ShopDesigner shopDesigner = shopDesignerRepo.findByIdAndIsActiveTrue(shopDesignerId);
        if (shopDesignerId == null) {
            throw new IllegalArgumentException("존재하지 않는 디자이너 입니다");
        }

        int likeCount = salonLikeRepo.countByLikeTypeAndTypeId(LikeType.DESIGNER, shopDesignerId);
        int reivewCount = reviewRepo.countByReservation_ShopDesigner_Id(shopDesignerId);


        // 디자이너 시술 카테고리 문자열 생성
        DesignerService ds = designerServiceRepo.findByShopDesignerId(shopDesignerId)
                .orElseThrow(() -> new IllegalArgumentException("디자이너의 시술 카데고리 정보가 없습니다"));

        List<ServiceCategory> assignedCategories = ds.getAssignedCategories();
        String expertiseText = assignedCategories.stream()
                .map(ServiceCategory::getLabel)
                .collect(Collectors.joining("   /   "));

        DesignerDetailDto dto = DesignerDetailDto.from(shopDesigner, likeCount, reivewCount);
        dto.setExpertise(expertiseText);

        return dto;
    }

    // 디자이너 리뷰, 전문 시술 분야 , 시술 리스트 ( 홈 섹션, 리뷰 섹션)
    public DesignerHomeDto getDesignerHomeInfo (Long shopDesignerId) {

        ShopDesigner shopDesigner = shopDesignerRepo.findByIdAndIsActiveTrue(shopDesignerId);
        if (shopDesignerId == null) {
            throw new IllegalArgumentException("존재하지 않는 디자이너 입니다");
        }

        // 추천 시술 목록
        List<ShopService> recommendedServices = shopServiceRepo.findByShopIdAndIsRecommendedTrue(shopDesignerId);
        List<ServiceForm> serviceForms = recommendedServices.stream()
                .map(ServiceForm :: from)
                .collect(Collectors.toList());

        // 리뷰수 조회
        int reviewCount = reviewRepo.countByReservation_ShopDesigner_Id(shopDesignerId);

        // 리뷰 이미지 썸네일 ( 최대 8장 최신순으로 )
        List<ReviewImage> thumbnails = reviewImageRepo.findTop8ByOrderByIdDesc();
        List<ReviewImageDto> thumbnailDtos = thumbnails.stream()
                .map(ReviewImageDto :: from)
                .collect(Collectors.toList());

        // 디자이너 리뷰 조회
        List<Review> reviews = reviewRepo.findByReservation_shopDesignerId(shopDesignerId);
        List<ReviewListDto> reviewListDtos = new ArrayList<>();

        for (Review review : reviews) {
            Reservation res = review.getReservation();
            Long memberId = res.getMember().getId();
            Long shopId = shopDesigner.getShop().getId();

            // 리뷰 이미지
            List<ReviewImageDto> imageDtos = getReviewImages(review.getId());

            // 방문횟수
            int visitCount = reservationRepo.countVisitByMemberAndShop(memberId,shopId);

            // 디자이너 답글
            ReviewReplyDto replyDto = null;
            if (review.getReplyComment() != null && review.getReplyAt() != null){
                replyDto = ReviewReplyDto.from(review);
            }
            ReviewListDto dto = ReviewListDto.from(review,shopDesigner,imageDtos,visitCount,replyDto);
            dto.setMemberName(res.getMember().getName());


            // 작성일 + 상대시간 표시
            LocalDate createdDate = review.getCreateAt() != null ? review.getCreateAt().toLocalDate() : LocalDate.now();

            String formattedDate = createdDate.format(DateTimeFormatter.ofPattern("yyyy.mm.dd"));
            long daysAgo = ChronoUnit.DAYS.between(createdDate, LocalDate.now());
            String daysAgoText = daysAgo == 0 ? "오늘" : daysAgo == 1 ? "1일 전" : daysAgo + "일 전";

            dto.setCreatedDateFormetted(formattedDate +  " · "  + daysAgoText);


            reviewListDtos.add(dto);
        }

        return DesignerHomeDto.from(serviceForms,reviewCount,thumbnailDtos,reviewListDtos);

    }

    private List<ReviewImageDto> getReviewImages(Long reviewId) {
        List<ReviewImage> imageList = reviewImageRepo.findByReviewId(reviewId);
        return imageList.stream()
                .filter(Objects::nonNull)
                .map(ReviewImageDto::from)
                .collect(Collectors.toList());
    }




    // 디자이너가 담당하는 카테고리와 시술목록을 카테고리로 묶어 반환하는 메서드 (디자이너 상세페이지 - 시술 리스트 섹션)
    public Map<Object, List<ServiceForm>> getServiceListByDesigner(Long shopDesignerId, Long shopId) {
        // 디자이너가 담당하는 카테고리 가져오기
        DesignerService ds = designerServiceRepo.findByShopDesignerId(shopDesignerId)
                .orElseThrow(() -> new IllegalArgumentException("해당 디자이너의 시술 정보가 없습니다"));

        List<ServiceCategory> assignedCategories = ds.getAssignedCategories();

        // 시술 필터링
        List<ShopService> filteredServices = shopServiceRepo.findByShopIdAndCategoryIn(shopId, assignedCategories);

        // 카테고리별로 묶기
        Map<Object, List<ServiceForm>> serviceMap = filteredServices.stream()
                .map(ServiceForm::from)
                .collect(Collectors.groupingBy(ServiceForm::getCategory, LinkedHashMap::new, Collectors.toList()));

        // 추천 시술 추가
        List<ShopService> recommendedList = shopServiceRepo.findByShopIdAndIsRecommendedTrue(shopId);
        List<ServiceForm> recommendedForms = recommendedList.stream()
                .map(ServiceForm::from)
                .collect(Collectors.toList());

        if (!recommendedForms.isEmpty()) {
            serviceMap.put("RECOMMENED", recommendedForms); // 문자열 키
        }

        return serviceMap;
    }


}
