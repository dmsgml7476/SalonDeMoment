package com.salon.dto.designer;

import com.salon.entity.Review;
import com.salon.entity.management.Designer;
import com.salon.entity.management.ShopDesigner;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
public class ReviewReplyDto {

    private Long reviewId; // 리뷰테이블 아이디
    private String designerName; // 디자이너이름
    private String designerPosition; // 디자이너 직급
    private String designerImg; // 디자이너 프로필 이미지
    private String replyComment; // 디자이너 답글 내용
    private LocalDateTime replyAt; //답글 작성 날짜



    // Review(Entity) -> ReviewReplyDto
    public static ReviewReplyDto from (Review review){

        if (review == null || review.getReservation() == null || review.getReservation().getShopDesigner() == null) {
            return null; // 방어적 처리
        }

        Designer designer = review.getReservation().getShopDesigner().getDesigner();


        ReviewReplyDto reviewReplyDto = new ReviewReplyDto();
        reviewReplyDto.setReviewId(review.getId());
        reviewReplyDto.setDesignerName(designer.getMember().getName());
        reviewReplyDto.setDesignerPosition(review.getReservation().getShopDesigner().getPosition());
        reviewReplyDto.setDesignerImg(review.getReservation().getShopDesigner().getDesigner().getImgUrl());
        reviewReplyDto.setReplyComment(review.getReplyComment());
        reviewReplyDto.setReplyAt(review.getReplyAt());


        return reviewReplyDto;
    }

}
