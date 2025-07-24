package com.salon.dto.designer;

import com.salon.dto.shop.ReviewListDto;
import com.salon.entity.management.ShopDesigner;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Getter
@Setter
public class DesignerDetailDto {

    private Long shopId; // 소속 미용실
    private String shopName; // 미용실 이름
    private Long designerId; // 디자이너 아이디
    private String originalName; // 이미지 원본 이름
    private String imgName; // 이미지 저장 이름
    private String imgUrl; // 이미지 경로
    private String designerName; // 디자이너 이름
    private LocalDate startAt; // 디자이너 시작일
    private String position; // 디자이너 직급
    private LocalTime scheduledStartTime; // 출근 시간
    private LocalTime scheduleEndTime; // 퇴근 시간
    private boolean isActive; // 재직 여부
    private int likeCount; // 디자이너별 찜 갯수
    private int reviewCount; // 디자이너별 리뷰 갯수
    private int rating; // 디자이너 평점
    private String expertise; // 디자이너 전문 시술 분야 (예 :" 커트 / 펌 / 염색 ")
    private String description; // 디자이너 소개


    private int careerYears; // 디자이너 연차 계산

    private MultipartFile designerProfile;
    private List<ReviewListDto> reviewList;


    // ShopDesigner(Entity) -> DesignerDetailDto
    public static DesignerDetailDto from (ShopDesigner shopDesigner, int likeCount, int reviewCount){
        DesignerDetailDto designerDetailDto = new DesignerDetailDto();

        designerDetailDto.setShopId(shopDesigner.getShop().getId());
        designerDetailDto.setDesignerId(shopDesigner.getId());
        designerDetailDto.setShopName(shopDesigner.getShop().getName());
        designerDetailDto.setDesignerName(shopDesigner.getDesigner().getMember().getName());
        designerDetailDto.setStartAt(shopDesigner.getDesigner().getStartAt());
        designerDetailDto.setPosition(shopDesigner.getPosition());
        designerDetailDto.setScheduledStartTime(shopDesigner.getScheduledStartTime());
        designerDetailDto.setScheduleEndTime(shopDesigner.getScheduledEndTime());
        designerDetailDto.setLikeCount(likeCount);
        designerDetailDto.setReviewCount(reviewCount);
        designerDetailDto.setDescription(shopDesigner.getDescription());

        return designerDetailDto;
    }
    
}
