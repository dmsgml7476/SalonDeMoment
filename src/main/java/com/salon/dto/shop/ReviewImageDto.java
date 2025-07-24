package com.salon.dto.shop;


import com.salon.entity.ReviewImage;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class ReviewImageDto {

    private Long reviewImageId; // 리뷰이미지 아이디
    private String originalName; // 이미지 원본 이름
    private String imgName; // 이미지 저장 이름
    private String imgUrl; // 이미지 저장 경로

    private MultipartFile imgFile; // 이미지 파일



    // ReviewImage(Entity) -> ReviewImageDto
    public static ReviewImageDto from (ReviewImage reviewImage){

        if (reviewImage == null) {
            throw new IllegalArgumentException("ReviewImage entity is null");
        }



        ReviewImageDto reviewImageDto = new ReviewImageDto();

        reviewImageDto.setReviewImageId(reviewImage.getId());
        reviewImageDto.setOriginalName(reviewImage.getOriginalName());
        reviewImageDto.setImgName(reviewImage.getImgName());
        reviewImageDto.setImgUrl(reviewImage.getImgUrl());


        return reviewImageDto;
    }
}
