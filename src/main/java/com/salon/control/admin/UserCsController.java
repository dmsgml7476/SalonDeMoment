package com.salon.control.admin;

import com.salon.config.CustomUserDetails;
import com.salon.constant.Role;
import com.salon.dto.BizStatusDto;
import com.salon.dto.admin.*;
import com.salon.entity.Member;
import com.salon.entity.management.Designer;
import com.salon.entity.management.ShopDesigner;
import com.salon.entity.management.master.Coupon;
import com.salon.entity.shop.Shop;
import com.salon.repository.management.DesignerRepo;
import com.salon.repository.management.ShopDesignerRepo;
import com.salon.repository.management.master.CouponRepo;
import com.salon.service.admin.AncService;
import com.salon.service.admin.CsService;
import com.salon.service.admin.DesApplyService;
import jakarta.servlet.http.HttpSession;
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
import java.util.*;

import static com.fasterxml.jackson.databind.type.LogicalType.Collection;

@Controller
@RequiredArgsConstructor
@RequestMapping("/cs")
public class UserCsController {
    private final AncService ancService;
    private final DesApplyService desApplyService;
    private final DesignerRepo designerRepo;
    private final ShopDesignerRepo shopDesignerRepo;
    @Value("${ocr.api}")
    private String ocrApiKey;
    @Value("${api.encodedKey}")
    private String encodedKey;
    private final RestTemplate restTemplate = new RestTemplate();
    private final CsService csService;
    private final CouponRepo couponRepo;
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
    // 공지 관련
    @GetMapping("")
    public String list(Model model,@AuthenticationPrincipal CustomUserDetails userDetails){
        Member member = userDetails.getMember();
        Role role = member.getRole();
        List<AncListDto> ancListDtoList;

        if(role == Role.ADMIN){
            ancListDtoList = ancService.list();
            model.addAttribute("isAdmin", true);
        } else if(role == Role.USER){
            ancListDtoList = ancService.findByRole(Role.USER);
        } else {
            ancListDtoList = ancService.findByRole(Role.DESIGNER);
        }
        model.addAttribute("ancListDto", ancListDtoList);
        model.addAttribute("userRole", role.name());
        return "admin/announcement";
    }
    @GetMapping("/detail")
    public String detail(@RequestParam("id") Long id, Model model){
        AncDetailDto ancDetailDto = ancService.detail(id);
        model.addAttribute("ancDetailDto", ancDetailDto);
        return "admin/announcementDetail";}
    // 디자이너 신청
    @GetMapping("/request")
    public String requestForm(Model model){
        model.addAttribute("applyDto", new ApplyDto());
        model.addAttribute("ocrApiKey", ocrApiKey);
        return "admin/apply";
    }
    @PostMapping("/request")
    public String request(@ModelAttribute ApplyDto applyDto,
                          @AuthenticationPrincipal CustomUserDetails userDetails,
                          @RequestParam(value="file", required = false) MultipartFile file,
                          HttpSession session,
                          Model model){
        System.out.println("✅ POST 요청 진입");
        System.out.println("applyNumber: " + applyDto.getApplyNumber());
        System.out.println("file: " + (file != null ? file.getOriginalFilename() : "없음"));
        Member member = userDetails.getMember();
        if(member == null){
            return "redirect:/";
        }
        try{
            desApplyService.Apply(applyDto, member, file);
            model.addAttribute("message", "디자이너 승인 요청이 완료되었습니다.");
            return "redirect:/";
        } catch (Exception e){
            model.addAttribute("error", "요청 처리 중 오류가 발생했습니다." + e.getMessage());
            model.addAttribute("ocrApiKey", ocrApiKey);
            return "admin/apply";
        }
    }
    // 고객 문의 관련
    @GetMapping("/question")
    public String question(Model model){
        model.addAttribute("csCreateDto", new CsCreateDto());
        return "admin/question";
    }


    @PostMapping("/question")
    public String questionSave(@AuthenticationPrincipal CustomUserDetails userDetails,
                               CsCreateDto csCreateDto,
                               @RequestParam("files") List<MultipartFile> files){
        Member member = userDetails.getMember();
        csService.questionSave(csCreateDto, member, files);
        return "redirect:/myPage/myQuestionList";
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


    @GetMapping("/shopApply")
    public String shopApply(Model model){
        model.addAttribute("applyDto", new ApplyDto());
        return "admin/shopApply";
    }

    @PostMapping("/shopApply")
    public String shopRegistration(@AuthenticationPrincipal CustomUserDetails userDetails,
                                   @ModelAttribute ApplyDto applyDto,
                                   Model model){

        System.out.println("폼 제출됨: " + applyDto.getApplyNumber());

        Member member = userDetails.getMember();
        try {
            csService.registration(applyDto, member);
        } catch(IllegalStateException e){
            model.addAttribute("applyDto", applyDto);
            model.addAttribute("errorMessage", e.getMessage());
            return "admin/shopApply";
        }
        return "redirect:/";
    }
    // 배너 신청 폼을 보여주는 GET 요청
    @GetMapping("/apply")
    public String applyForm(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        Member member = userDetails.getMember();

        Shop shop = null;
        List<Coupon> activeCouponList = Collections.emptyList();

        if (member.getRole().equals(Role.MAIN_DESIGNER)) {
            Optional<Designer> designerOptional = designerRepo.findByMemberId(member.getId());
            if (designerOptional.isPresent()) {
                Optional<ShopDesigner> shopDesignerOptional = shopDesignerRepo.findByDesignerId(designerOptional.get().getId());
                System.out.println("designer : " + designerOptional.get().getId());
                if (shopDesignerOptional.isPresent()) {
                    shop = shopDesignerOptional.get().getShop();
                    activeCouponList = couponRepo.findByShopAndIsActiveTrue(shop); // 샵에 연결된 활성 쿠폰 조회
                } else {
                    model.addAttribute("error", "해당 디자이너는 소속된 미용실이 없습니다.");
                    model.addAttribute("couponList", activeCouponList);
                    return "admin/bannerApply";
                }
            } else {
                model.addAttribute("error", "해당 회원과 연결된 디자이너 정보가 없습니다.");
                model.addAttribute("couponList", activeCouponList);
                return "admin/bannerApply";
            }
        } else {
            // MAIN_DESIGNER 외 다른 모든 역할 (ADMIN 포함)은 권한이 없습니다.
            model.addAttribute("error", "쿠폰 배너 신청 권한이 없습니다. (디자이너만 가능)");
            model.addAttribute("couponList", activeCouponList);
            return "admin/bannerApply";
        }

        model.addAttribute("couponList", activeCouponList);
        return "admin/bannerApply";
    }
    // 쿠폰 배너 신청 폼 데이터를 처리하는 POST 요청
    @PostMapping("/apply")
    public String bannerApply(@ModelAttribute BannerApplyDto bannerApplyDto,
                              @AuthenticationPrincipal CustomUserDetails userDetails,
                                @RequestParam("file") MultipartFile file){
        Long memberId = userDetails.getMember().getId();
        csService.applyBanner(bannerApplyDto, memberId, file);
        return "redirect:/";
    }
    @GetMapping("/bannerList")
    public String bannerList(Model model){
        List<CouponBannerListDto> couponBannerListDtoList = csService.bannerList();
        model.addAttribute("couponBannerListDtoList", couponBannerListDtoList);
        return "admin/bannerList";
    }
}
