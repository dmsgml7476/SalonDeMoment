package com.salon.dto.user;

import com.salon.entity.Review;
import com.salon.util.DateTimeUtil;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class MyReviewDetailDto {
    private Long id; //reviewId
    private MyReviewListDto myReviewListDto;  // 리뷰 리스트 dto
    private String designerName;  // 디자이너 이름
    private String position;  // 디자이너 직급
    private String shopName;  // 샵 이름
    private String comment; //리뷰 내용

    // 리뷰 댓글

    private String designerImgUrl;
    private String replyComment;
    private String replyAt;

    public static MyReviewDetailDto from(Review review, MyReviewListDto myReviewListDto) {
        MyReviewDetailDto dto = new MyReviewDetailDto();

        dto.setId(review.getId());
        dto.setMyReviewListDto(myReviewListDto);
        dto.setDesignerName(review.getReservation().getShopDesigner().getDesigner().getMember().getName());
        dto.setDesignerImgUrl(review.getReservation().getShopDesigner().getDesigner().getImgUrl());
        dto.setPosition(review.getReservation().getShopDesigner().getPosition());
        dto.setShopName(review.getReservation().getShopDesigner().getShop().getName());
        dto.setComment(review.getComment());

        dto.setReplyComment(review.getReplyComment());
        dto.setReplyAt(
                review.getReplyAt() != null
                        ? DateTimeUtil.getTimeAgo(review.getReplyAt(), true)
                        : null
        );

        return dto;
    }



}
