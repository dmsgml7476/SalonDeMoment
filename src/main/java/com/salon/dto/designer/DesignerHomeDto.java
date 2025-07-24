package com.salon.dto.designer;


import com.salon.dto.management.ServiceForm;
import com.salon.dto.shop.ReviewImageDto;
import com.salon.dto.shop.ReviewListDto;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class DesignerHomeDto {

    private List<ServiceForm> serviceList; // 디자이너가 시술 가능한 시술 리스트
    private int reviewCount; // 디자이너 리뷰 갯수
    private List<ReviewImageDto> thumbnails; // 리뷰 썸네일
    private List<ReviewListDto> recentReviews; // 해당 디자이너의 리뷰 목록



    public static DesignerHomeDto from(List<ServiceForm> serviceList, int reviewCount, List<ReviewImageDto> thumbnails, List<ReviewListDto> recentReviews) {
        DesignerHomeDto dto = new DesignerHomeDto();
        dto.setServiceList(serviceList);
        dto.setReviewCount(reviewCount);
        dto.setThumbnails(thumbnails);
        dto.setRecentReviews(recentReviews);
        return dto;
    }

}
