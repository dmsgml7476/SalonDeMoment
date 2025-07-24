package com.salon.dto.designer;

import com.salon.constant.ServiceCategory;
import com.salon.entity.management.Designer;
import com.salon.entity.management.ShopDesigner;
import com.salon.entity.management.master.DesignerService;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class DesignerListDto {

    private Long id; // 디자이너 id
    private Long shopId; // 소속미용실 id
    private String name; // 디자이너 이름
    private int workingYear; // 디자이너 연차
    private String position; // 디자이너 직급
    private String description; // 디자이너 소개
    private String imgUrl; // 디자이너 프로필 이미지
    private int reviewCount; // 디자이너 리뷰 갯수
    private int likeCount; // 디자이너 찜 갯수
    private int rating; // 디자이너 평점

    private String profileSummary; // 디자이너 전문 시술 분야 + 연차

    // ShopDesigner(Entity) -> DesignerListDto
    public static DesignerListDto from (ShopDesigner shopDesigner, int likeCount, int reviewCount, DesignerService designerService){
        DesignerListDto designerListDto = new DesignerListDto();

        designerListDto.setId(shopDesigner.getId());
        designerListDto.setShopId(shopDesigner.getShop().getId());

        String name = shopDesigner.getDesigner().getMember().getName();
        String position = shopDesigner.getPosition();
        designerListDto.setName(position + " " + name);

        int workingYears = shopDesigner.getDesigner().getWorkingYears();
        designerListDto.setWorkingYear(workingYears);
        designerListDto.setPosition(position);
        designerListDto.setImgUrl(shopDesigner.getDesigner().getImgUrl());
        designerListDto.setReviewCount(reviewCount);
        designerListDto.setLikeCount(likeCount);

        if (designerService != null) {
            List<ServiceCategory> assignedCategories = designerService.getAssignedCategories();
            List<String> categoryLabels = new ArrayList<>();

            for (ServiceCategory category : assignedCategories) {
                categoryLabels.add(category.getLabel());
            }

            String categoriesString = String.join(", ", categoryLabels);

            if (!categoriesString.isEmpty()) {
                categoriesString += " 담당";
            }
            // 커트, 염색, 펌 담당 (n년차)
            designerListDto.setProfileSummary(categoriesString + " (" + workingYears + "년차)");
        } else {
            // n년차
            designerListDto.setProfileSummary(workingYears + "년차");
        }



        return designerListDto;
    }
}