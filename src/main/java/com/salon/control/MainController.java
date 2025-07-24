package com.salon.control;

import com.salon.config.CustomUserDetails;
import com.salon.constant.ServiceCategory;
import com.salon.dto.shop.RecommendDesignerDto;
import com.salon.dto.shop.ShopListDto;
import com.salon.dto.user.*;
import com.salon.service.WebNotificationService;
import com.salon.service.shop.SalonService;
import com.salon.service.user.CompareService;
import com.salon.service.user.KakaoMapService;
import com.salon.service.user.MainCouponBannerService;
import com.salon.service.user.MemberService;
import com.salon.util.DistanceUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.graphql.GraphQlProperties;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Controller
public class MainController {
    @Value("${kakao.maps.api.key}")
    private String kakaoMapsKey;

    @Value("${kakao.rest.api.key}")
    private String kakaoRestKey;

    private final MemberService memberService;
    private final KakaoMapService kakaoMapService;
    private final SalonService salonService;
    private final CompareService compareService;
    private final WebNotificationService webNotificationService;
    private final MainCouponBannerService mainCouponBannerService;


    @GetMapping("/")
    public String mainpage(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        if (userDetails == null) {
            model.addAttribute("userAgreeLocation", false);
            model.addAttribute("currentUserId", null);
        } else {

            String name = userDetails.getMember().getName();
            model.addAttribute("name", name);

            boolean isAdmin = userDetails.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            model.addAttribute("isAdmin", isAdmin);


            // 로그인 사용자 정보 추가
            model.addAttribute("userAgreeLocation", userDetails.getMember().isAgreeLocation());
            model.addAttribute("currentUserId", userDetails.getMember().getId());
        }

        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        System.out.println("principal 클래스: " + principal.getClass());
        System.out.println("principal 값: " + principal);

        model.addAttribute("kakaoMapsKey", kakaoMapsKey);

        return "/mainpage";
    }

    // 위치 정보 제공 동의

    @PatchMapping("/api/member/location-consent")
    public ResponseEntity<Void> updateLocationConsent(@AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = userDetails.getMember().getId();
        memberService.updateAgreeLocation(userId); // 동의 처리
        return ResponseEntity.ok().build();
    }


    // 위도경도를 주소로 변환
    @GetMapping("/api/coord-to-address")
    public ResponseEntity<?> getAddressFormCoords(@RequestParam BigDecimal x, @RequestParam BigDecimal y) {
        try {
            UserLocateDto location = kakaoMapService.getUserAddress(x, y);
            return ResponseEntity.ok(location);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }



    // 메인 지도에 표시할 샵 목록 불러오기
    @GetMapping("/api/shops")
    @ResponseBody
    public List<ShopMapDto> getShopsForMap(@RequestParam BigDecimal lat, @RequestParam BigDecimal lon) {
        return salonService.getAllShopsForMap(lat, lon);
    }

    // 배너 불러오기
    @GetMapping("/api/main-banners")
    @ResponseBody
    public ResponseEntity<List<MainCouponBannerDto>> getMainPageBanners(@RequestParam String region) {
        List<MainCouponBannerDto> banners = mainCouponBannerService.getApprovedBannersByRegion(region);
        return ResponseEntity.ok(banners);
    }

    // 추천 샵 불러오기
    @GetMapping("/api/recommend-shops")
    @ResponseBody
    public List<ShopRecommendListDto> getRecommendedShopsByRegion(
            @RequestParam String region,
            @RequestParam(required = false) BigDecimal lat,
            @RequestParam(required = false) BigDecimal lon
    ) {
        return salonService.getRecommendedShops(region, lat, lon);
    }

    // 디자이너 추천
    @GetMapping("/api/salon/designers/recommend")
    @ResponseBody
    public ResponseEntity<List<RecommendDesignerDto>> getRecommendedDesigners(
            @RequestParam("region") String region1depth
    ) {
        List<RecommendDesignerDto> list = salonService.getRecommendedDesignersByRegion(region1depth);
        return ResponseEntity.ok(list);
    }



    // 헤어샵 목록 페이지
    @GetMapping("/shopList")
    public String shopListPage(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        if(userDetails != null) {
            boolean agree = userDetails.getMember().isAgreeLocation();

            model.addAttribute("userAgreeLocation", agree);
            model.addAttribute("currentUserId", userDetails.getMember().getId());
        } else {
            // 비회원
            model.addAttribute("userAgreeLocation", false);
            model.addAttribute("currentUserId", null);
        }

        return "/user/shopList";
    }

    @GetMapping("/api/shop-list")
    @ResponseBody
    public List<ShopListDto> getShopListByRegion(
            @RequestParam String region,
            @RequestParam BigDecimal lat,
            @RequestParam BigDecimal lon,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "distance") String sort

    ) {
        try {
            return salonService.getShopByRegion(region, lat, lon, page, size, sort);
        } catch (Exception e) {
            e.printStackTrace();  // 콘솔에 에러 출력
            throw e; // 다시 던져서 클라이언트에 500 응답
        }
    }

    @PostMapping("/api/saveSelectedShops")
    @ResponseBody
    public ResponseEntity<?> saveSelectedShopsToSession(@RequestBody List<Long> shopIds, HttpSession session) {
        if (shopIds == null || shopIds.isEmpty()) {
            return ResponseEntity.badRequest().body("샵 ID 목록이 비어있습니다.");
        }

        // 최대 3개까지만 저장
        List<Long> limitedShopIds = shopIds.size() > 3 ? shopIds.subList(0, 3) : shopIds;

        session.setAttribute("selectedShopIds", limitedShopIds);
        return ResponseEntity.ok().build();
    }


    @GetMapping("/compare")
    public String comparePage(HttpSession session, Model model,
                              @RequestParam(required = false) BigDecimal userLat,
                              @RequestParam(required = false) BigDecimal userLon) {

        //List<Long> selectedShopIds = (List<Long>) session.getAttribute("selectedShopIds"); 이걸로 하려다
        //타입 안정성이 보장 되지 않아서 경고 멘트가 떠서
        // 타입 체크 및 안전한 형변환을 적용함.
        Object obj = session.getAttribute("selectedShopIds");

        List<Long> selectedShopIds = new ArrayList<>();
        if (obj instanceof List<?>) {
            for (Object o : (List<?>) obj) {
                if (o instanceof Long) {
                    selectedShopIds.add((Long) o);
                } else if (o instanceof Integer) { // 만약 Integer로 들어온 경우 대응
                    selectedShopIds.add(((Integer) o).longValue());
                }
            }
        }

        if (selectedShopIds.isEmpty()) {
            return "redirect:/shopList";
        }

        List<ShopCompareResultDto> compareResults = compareService.getCompareResults(
                selectedShopIds,
                userLat != null ? userLat : BigDecimal.ZERO,
                userLon != null ? userLon : BigDecimal.ZERO
        );
        model.addAttribute("compareResults", compareResults);
        model.addAttribute("serviceCategories", ServiceCategory.values());


        return "/user/compare";
    }

    @GetMapping("/login")
    public  String loginPage(@RequestParam(value = "error", required = false) String error, Model model) {
        if(error != null) {
            model.addAttribute("loginErrorMsg", "옳지 않은 아이디나 비밀번호입니다.");
        }
        return "/user/login";
    }


    @GetMapping("/signUp")
    public String signUpPage(Model model) {
        model.addAttribute("signUpDto", new SignUpDto());
        return "/user/signUp";
    }

    @PostMapping("/signUp")
    public String signUp(@ModelAttribute SignUpDto dto, HttpSession session, BindingResult result, Model model) {

        Boolean verified = (Boolean) session.getAttribute("authSuccess");

        if (verified == null || !verified) {
            result.rejectValue("email", "NotVerified", "이메일 인증을 완료해주세요.");
            return "user/signUpForm";
        }

        // 인증이 완료되었으면 회원 가입 진행
        memberService.register(dto);

        session.removeAttribute("authSuccess"); // 인증 정보 제거 (1회용)
        return "redirect:/login";
    }




    // js에서 보낸 아이디 찾기 요청 처리

    @GetMapping("/check-id")
    @ResponseBody
    public Map<String, Boolean> checkLoginId(@RequestParam String loginId) {
       boolean exists = memberService.existsByLoginId(loginId);
       return Map.of("exists", exists);
    }

    // 웹 알림 읽음 표시
    @PostMapping("/api/notification/read")
    @ResponseBody
    public ResponseEntity<?> markAsRead(@RequestBody NotificationReadDto dto) {
        webNotificationService.markAsReadByTarget(dto.getWebTarget(), dto.getTargetId());
        return ResponseEntity.ok().build();
    }


    // 검색

    @GetMapping("/api/shop-list/search")
    @ResponseBody
    public List<ShopListDto> searchShops(@RequestParam String keyword) {
        return salonService.searchByName(keyword);
    }



}
