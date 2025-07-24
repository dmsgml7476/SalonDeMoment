package com.salon.control;


import com.salon.dto.designer.DesignerDetailDto;
import com.salon.dto.designer.DesignerHomeDto;
import com.salon.service.designer.DesignerDetailService;
import com.salon.service.user.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;
import java.time.LocalDate;
import java.time.Period;

@Controller
@RequiredArgsConstructor
@RequestMapping("/designerProfile")
public class DesignerController {


    private final DesignerDetailService designerDetailService;


    @GetMapping("/{shopDesignerId}")
    public String showDesignerDetail (@PathVariable("shopDesignerId") Long shopDesignerId,Model model ){


        // 디자이너 기본 정보 (상단 프로필 영역)
        DesignerDetailDto detailDto = designerDetailService.getDesignerDetail(shopDesignerId);

        // 디자이너 홈/ 리뷰 정보 (홈 탭 + 리뷰 요약 영역 + 리뷰리스트)
        DesignerHomeDto homeDto = designerDetailService.getDesignerHomeInfo(shopDesignerId);

        // 디자이너 시술 리스트 (메뉴 탭 영역)
        Long shopId = detailDto.getShopId();
        var serviceMap = designerDetailService.getServiceListByDesigner(shopDesignerId,shopId);

        // 디자이너 연차 계산
        int careerYears = Period.between(detailDto.getStartAt(), LocalDate.now()).getYears();
        detailDto.setCareerYears(careerYears);

        model.addAttribute("detail", detailDto);
        model.addAttribute("home", homeDto);
        model.addAttribute("serviceMap", serviceMap);


        return "designer/designerProfile";
    }




}
