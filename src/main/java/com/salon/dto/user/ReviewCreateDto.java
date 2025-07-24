package com.salon.dto.user;

import com.salon.entity.Review;
import com.salon.entity.shop.Reservation;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;


import java.util.List;


@Getter @Setter
public class ReviewCreateDto {


    private int rating;
    private String comment;
    private Long reservationId;
    private List<MultipartFile> reviewImages;

    public Review to (Reservation reservation) {
        Review review = new Review();

        review.setReservation(reservation);
        review.setRating(this.rating);
        review.setComment(this.comment);
        review.setCreateAt(java.time.LocalDateTime.now());

        return review;
    }

}
