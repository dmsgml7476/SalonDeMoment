package com.salon.control;

import com.salon.constant.ServiceCategory;
import com.salon.dto.designer.DesignerListDto;
import com.salon.dto.management.ServiceForm;
import com.salon.dto.shop.ReviewListDto;
import com.salon.dto.shop.ShopDetailDto;
import com.salon.dto.shop.ShopServiceSectionDto;
import com.salon.service.shop.ShopDetailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/shop")
public class ShopController {

    private final ShopDetailService shopDetailService;


    @GetMapping("")
    public String getShop(){

        return "shop/shopDetail";
    }

    // 미용실 상세 페이지
    @GetMapping("/{shopId}")
    public String getShopDetail(@PathVariable("shopId") Long shopId,@RequestParam(required = false)ServiceCategory category,@RequestParam(required = false, defaultValue = "lastest") String sort,Model model){

        // 홈섹션 (상세페이지에서 기본적으로 보이는 정보들)

        // 미용실 기본 정보
        ShopDetailDto shopDetail = shopDetailService.getShopDetail(shopId);
        model.addAttribute("shop", shopDetail);

        // 추천시술
        List<ServiceForm> recommended = shopDetailService.getRecommededService(shopId);
        model.addAttribute("recommendedService", recommended);

        // 디자이너 목록
        List<DesignerListDto> designerLists = shopDetailService.getDesignersByShopId(shopId);
        model.addAttribute("designerLists", designerLists);

        // 리뷰 목록
        List<ReviewListDto> reviewLists = shopDetailService.getFilteredReviews(designerLists,category,sort);
        model.addAttribute("reviewList", reviewLists);


        // 시술 섹션 (시술 카테고리별 시술 목록 출력)
        ShopServiceSectionDto serviceSection = shopDetailService.getShopServiceSections(shopId);
        model.addAttribute("serviceSection", serviceSection);

        // 디자이너 섹션 (디자이너 메뉴 클릭시 출력)
        List<DesignerListDto> designerListsSection = shopDetailService.getDesignersByShop(shopId);
        model.addAttribute("designerListsSection", designerListsSection);

        // 리뷰 8개 썸네일
        List<String> reviewImageUrls = shopDetailService.getThumbReviewUrl(shopId);
        model.addAttribute("reviewImgUrls", reviewImageUrls);


        // 부가적으로 필요한 모델 바인딩

        // category가 null이면 Map에서 첫 번째 key를 기본값으로 사용
        if (category == null && !serviceSection.getCategoryMap().isEmpty()) {
            category = serviceSection.getCategoryMap().keySet().iterator().next();
        }
        model.addAttribute("selectCategory", category);


        model.addAttribute("selectedSort", sort);


        return "shop/shopDetail";

    }
}
