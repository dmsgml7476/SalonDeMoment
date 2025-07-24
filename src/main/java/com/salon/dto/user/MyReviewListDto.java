package com.salon.dto.user;

import com.salon.dto.shop.ReviewImageDto;
import com.salon.entity.Review;
import com.salon.entity.admin.WebNotification;
import com.salon.util.DateTimeUtil;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;


@Getter
@Setter
public class MyReviewListDto {
    private Long id;  // 리뷰 아이디
    private List<ReviewImageDto> reviewImageDtoList;
    // 리뷰 이미지 없을시
    private float rating;  // 평점
    private String date;  // 리뷰 작성 날짜
    private String serviceName;  // 시술 이름
    private Long reservationId;

    private boolean isRead; // 알림 읽음여부


    public static MyReviewListDto from(Review review, List<ReviewImageDto> reviewImageDtos,WebNotification webNotification) {
        MyReviewListDto dto = new MyReviewListDto();
        dto.setId(review.getId());
        dto.setRating(review.getRating());

        dto.setDate(DateTimeUtil.getTimeAgo(review.getCreateAt(), true));

        dto.setServiceName(review.getReservation().getServiceName());
        dto.setReviewImageDtoList(reviewImageDtos);

        dto.setRead(webNotification == null || webNotification.isRead());
        dto.setReservationId(review.getReservation().getId());

        return dto;
    }
}
