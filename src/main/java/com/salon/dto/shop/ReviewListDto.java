package com.salon.dto.shop;

import com.salon.constant.ServiceCategory;
import com.salon.dto.designer.ReviewReplyDto;
import com.salon.entity.Member;
import com.salon.entity.Review;
import com.salon.entity.management.Designer;
import com.salon.entity.management.ShopDesigner;
import com.salon.entity.management.master.ShopService;
import com.salon.entity.shop.Reservation;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class ReviewListDto {

    private Long id; //리뷰테이블 아이디
    private String memberName; // 리뷰작성자 이름
    private LocalDateTime createAt; // 리뷰작성 날짜
    private String serviceName; // 시술 이름
    private int visitCount; // 사용자 방문 횟수
    private int rating; // 평점
    private String comment; // 리뷰내용
    private String designerName; // 시술한 디자이너 이름
    private int designerWorkingYears; // 시술한 디자이너 연차
    private String createdDateFormetted; // 리뷰 날짜

    private String category; // 카테고리

    private List<ReviewImageDto> imageDtos = new ArrayList<>(); // 리뷰 이미지
    private ReviewReplyDto reply; // 디자이너 답글 dto

    // Review(Entity) -> ReviewListDto
    public static ReviewListDto from (Review review, ShopDesigner shopDesigner, List<ReviewImageDto> reviewImgs, int visitCount, ReviewReplyDto replyDto){
        ReviewListDto reviewListDto = new ReviewListDto();


        reviewListDto.setId(review.getId());
        reviewListDto.setServiceName(review.getReservation().getShopService().getName());
        reviewListDto.setCreateAt(review.getCreateAt());
        reviewListDto.setRating(review.getRating());
        reviewListDto.setComment(review.getComment());
        reviewListDto.setImageDtos(reviewImgs != null ? reviewImgs : new ArrayList<>());
        reviewListDto.setVisitCount(visitCount);
        reviewListDto.setDesignerName(shopDesigner.getDesigner().getMember().getName());
        reviewListDto.setDesignerWorkingYears(shopDesigner.getDesigner().getWorkingYears());
        reviewListDto.setMemberName(review.getReservation().getMember().getName());
        reviewListDto.setCategory(review.getReservation().getShopService().getCategory().getLabel());

        // 시술명
        Reservation res = review.getReservation();
        ShopService service = res != null ? res.getShopService() : null;
        reviewListDto.setServiceName(service != null ? service.getName() : null);

        // 디자이너 정보
        String designerInfo = "By " + shopDesigner.getPosition() + " " + shopDesigner.getDesigner().getMember().getName();
        reviewListDto.setDesignerName(designerInfo);
        reviewListDto.setDesignerWorkingYears(shopDesigner.getDesigner().getWorkingYears());

        //  디자이너 답글
        reviewListDto.setReply(replyDto); // null이면 null로 세팅


        return reviewListDto;
    }


}
