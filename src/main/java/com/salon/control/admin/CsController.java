package com.salon.control.admin;

import com.salon.config.CustomUserDetails;
import com.salon.constant.Role;
import com.salon.constant.WebTarget;
import com.salon.dto.BizStatusDto;
import com.salon.dto.admin.*;
import com.salon.entity.Member;
import com.salon.entity.admin.Apply;
import com.salon.entity.admin.CsCustomer;
import com.salon.entity.management.Designer;
import com.salon.entity.management.ShopDesigner;
import com.salon.entity.management.master.Coupon;
import com.salon.entity.shop.Shop;
import com.salon.repository.MemberRepo;
import com.salon.repository.WebNotificationRepo;
import com.salon.repository.management.DesignerRepo;
import com.salon.repository.management.ShopDesignerRepo;
import com.salon.repository.management.master.CouponRepo;
import com.salon.repository.shop.ShopRepo;
import com.salon.service.WebNotificationService;
import com.salon.service.admin.AncService;
import com.salon.service.admin.CsService;
import com.salon.service.admin.DesApplyService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.nio.file.WatchEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/cs")
public class CsController {
    @Value("${api.encodedKey}")
    private String encodedKey;

    private final CouponRepo couponRepo;
    private final ShopDesignerRepo shopDesignerRepo;
    private final DesignerRepo designerRepo;
    private final ShopRepo shopRepo;
    private final AncService ancService;
    private final RestTemplate restTemplate = new RestTemplate();
    private final WebNotificationService webNotificationService;
    private final MemberRepo memberRepo;


    @GetMapping("/api/bizCheck")
    public ResponseEntity<?> check(@RequestParam String bizNo) {
        System.out.println("bizNo: " + bizNo);
        System.out.println("✅ encodedKey: " + encodedKey);
        // 요청 JSON 구성
        Map<String, Object> body = new HashMap<>();
        body.put("b_no", List.of(bizNo));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        URI uri = UriComponentsBuilder.fromHttpUrl("https://api.odcloud.kr/api/nts-businessman/v1/status")
                .queryParam("serviceKey", encodedKey)
                .build(true)  // encodedKey가 이미 인코딩되어 있음
                .toUri();

        try {
            ResponseEntity<BizStatusDto> response = restTemplate.exchange(
                    uri,
                    HttpMethod.POST,
                    entity,
                    BizStatusDto.class
            );
            System.out.println("API 응답: " + response.getBody());
            return ResponseEntity.ok(response.getBody());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("API 오류: " + e.getMessage());
        }

    }

    private final CsService csService;

    private final DesApplyService desApplyService;
    @GetMapping("")
    public String list(Model model){
        List<AncListDto> ancListDtoList = ancService.list();
        model.addAttribute("ancListDto", ancListDtoList);
        return "admin/announcement";
    }

    @GetMapping("/questionList")
    public String questionList(Model model, @AuthenticationPrincipal CustomUserDetails userDetails){
        List<CsListDto> csListDtoList = csService.List();
        Member member = userDetails.getMember();
        if(member.getRole() == Role.ADMIN){
            csListDtoList = csService.findAll();
        } else {
            csListDtoList = csService.findByMember(member);
        }
        model.addAttribute("csListDtoList", csListDtoList);
        return "admin/csList";
    }

    @GetMapping("/reply")
    public String reply(@AuthenticationPrincipal CustomUserDetails userDetails,
                        @RequestParam Long id,
                        Model model){
        CsCreateDto csCreateDto = csService.create(id);
        CsListDto csListDto = csService.list(id);
        CsDetailDto csDetailDto = csService.detail(id);
        boolean isAdmin = userDetails.getMember().getRole().name().equals("ADMIN");
        model.addAttribute("isAdmin", isAdmin);
        model.addAttribute("csCreateDto", csCreateDto);
        model.addAttribute("csListDto", csListDto);
        model.addAttribute("csDetailDto", csDetailDto);
        model.addAttribute("isAdmin", isAdmin);
        return "admin/reply";
    }

    @PostMapping("/reply")
    public String replySave(@AuthenticationPrincipal CustomUserDetails userDetails,
                            @ModelAttribute CsDetailDto csDetailDto){
        Long id = csDetailDto.getId();
        CsCreateDto csCreateDto = csService.create(id);
        CsListDto csListDto = csService.list(id);
        csDetailDto.setLoginId(userDetails.getUsername());


        // 웹 알림 관련
        Long receiverId = csService.replySave(csDetailDto, csCreateDto, csListDto);

        webNotificationService.notify(
                receiverId,
                "고객문의 답변이 도착했습니다",
                WebTarget.CS,
                id
        );

        return "redirect:/admin/cs/questionList";
    }

    @GetMapping("/shopList")
    public String shopList(Model model){
        List<Apply> list = csService.listShop();
        System.out.println("샵 신청 목록 수: " + list.size());
        for (Apply a : list) {
            System.out.println("신청: " + a.getApplyNumber() + " / 타입: " + a.getApplyType() + " / 상태: " + a.getStatus());
        }
        model.addAttribute("shopApplyList", list);
        return "admin/shopList";
    }

    @PostMapping("/approve/{id}")
    public String approveShop(@PathVariable Long id,
                              @AuthenticationPrincipal CustomUserDetails userDetails){
        Long receiverId = csService.approveShop(id, userDetails.getMember());

        webNotificationService.notify(
                receiverId,
                "미용실 신청이 승인되었습니다.",
                WebTarget.SHOPAPPROVE,
                id
        );


        return "redirect:/admin/cs/shopList";
    }

    @PostMapping("/reject/{id}")
    public String rejectShop(@PathVariable Long id,
                             @AuthenticationPrincipal CustomUserDetails userDetails){
        Long receiverId = csService.rejectShop(id, userDetails.getMember());

        webNotificationService.notify(
                receiverId,
                "미용실 신청이 거절되었습니다.",
                WebTarget.SHOPREJECT,
                id
        );

        return "redirect:/admin/cs/shopList";
    }

    @GetMapping("/bannerList")
    public String bannerList(Model model){
        List<CouponBannerListDto> couponBannerListDtoList = csService.bannerList();
        model.addAttribute("couponBannerListDtoList", couponBannerListDtoList);
        return "admin/bannerList";
    }

    @GetMapping("/bannerDetail")
    public String bannerDetail(@RequestParam Long id, Model model){
        CouponBannerDetailDto couponBannerDetailDto = csService.bannerDetail(id);
        model.addAttribute("couponBannerDetailDto", couponBannerDetailDto);
        return "admin/bannerDetail";
    }

    @PostMapping("/couponBanner/approve")
    public String bannerApprove(@RequestParam Long id, @AuthenticationPrincipal CustomUserDetails userDetails){
        Member admin = userDetails.getMember();
        Long receiverId = csService.bannerApprove(id, admin);



        webNotificationService.notify(
                receiverId,
                "쿠폰 배너 신청이 승낙되었습니다",
                WebTarget.BANNER,
                id
        );
        return "redirect:/admin/cs/bannerList";
    }

    @PostMapping("/couponBanner/reject")
    public String bannerReject(@RequestParam Long id, @AuthenticationPrincipal CustomUserDetails userDetails){
        Member admin = userDetails.getMember();
        Long receiverId = csService.bannerReject(id, admin);

        webNotificationService.notify(
                receiverId,
                "쿠폰 배너 신청이 거절되었습니다",
                WebTarget.BANNER,
                id
        );

        return "redirect:/admin/cs/bannerList";
    }

}
