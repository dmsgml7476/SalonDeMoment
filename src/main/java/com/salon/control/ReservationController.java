package com.salon.control;


import com.salon.config.CustomUserDetails;
import com.salon.constant.ServiceCategory;
import com.salon.constant.WebTarget;
import com.salon.dto.designer.DesignerListDto;
import com.salon.dto.management.MemberCouponDto;
import com.salon.dto.shop.*;
import com.salon.dto.user.ReReservationFormDto;
import com.salon.entity.Member;
import com.salon.entity.admin.WebNotification;
import com.salon.entity.management.ShopDesigner;
import com.salon.entity.shop.Reservation;
import com.salon.repository.MemberRepo;
import com.salon.repository.WebNotificationRepo;
import com.salon.repository.management.ShopDesignerRepo;
import com.salon.repository.management.master.ShopServiceRepo;
import com.salon.service.WebNotificationService;
import com.salon.service.shop.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/reservation")
public class ReservationController {


    private final ReservationService reservationService;
    private final ShopDesignerRepo shopDesignerRepo;
    private final MemberRepo memberRepo;
    private final WebNotificationService webNotificationService;



    // 예약 작성 페이지 Get매핑
    @GetMapping("/write")
    public String writeReservation(@RequestParam Long shopId, @RequestParam(required = false) Long shopDesignerId, @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)LocalDate date,Principal principal ,Model model) {


        // 로그인 사용자 정보 바인딩
        if (principal != null) {
            String loginId = principal.getName();
            Member member = memberRepo.findByLoginId(loginId);
            model.addAttribute("member",member);
        }

        model.addAttribute("shopId", shopId);
        model.addAttribute("selectedDesignerId", shopDesignerId);

        // 디자이너 목록
        List<DesignerListDto> designerList = reservationService.getDesignerList(shopId);
        model.addAttribute("designerList", designerList);

        List<DesignerServiceCategoryDto> serviceCategories = new ArrayList<>();
        if(shopDesignerId == null){ // 디자이너 선택안될땐 시술 다 보여주기
            serviceCategories = reservationService.getAllServiceCategories(shopId);
        } else{ // 디자이너 선택시 해당 디자이너의 시술 목록만
            ShopDesigner shopDesigner = shopDesignerRepo.findByIdAndIsActiveTrue(shopDesignerId);
            model.addAttribute("designer", shopDesigner);
            serviceCategories = reservationService.getDesignerServiceCategories(shopDesignerId);
        }
        model.addAttribute("serviceCategories", serviceCategories);

        // 시술 가능한 목록 빼오기
        Set<ServiceCategory> allCategories = new HashSet<>();
        for (DesignerServiceCategoryDto dto : serviceCategories) {
            allCategories.add(dto.getCategory());
        }
        model.addAttribute("assignedCategories", allCategories);

        if (shopDesignerId != null && date != null) {
            AvailableTimeSlotDto timeSlotDto = reservationService.getAvailableTimeSlots(shopDesignerId, date);
            model.addAttribute("availableTimeSlot", timeSlotDto);
        }



        return "shop/reservationSelect";
    }

    // 디자이너 ID로 시술 목록을 가져오는 API (AJAX 요청용)
    @GetMapping("/designers/{designerId}/services") // URL 경로에 designerId 포함
    @ResponseBody
    public List<DesignerServiceCategoryDto> getDesignerServicesByPath(@PathVariable Long designerId) {
        return reservationService.getDesignerServiceCategories(designerId);
    }


    // 매장 전체 시술 목록을 가져오는 API (AJAX 요청용)
    @GetMapping("/shop-services")
    @ResponseBody // JSON 형태로 데이터를 반환
    public List<DesignerServiceCategoryDto> getShopServices(@RequestParam Long shopId) {
        return reservationService.getAllServiceCategories(shopId);
    }

    // 날짜 시간 출력
    @GetMapping("/designers/{designerId}/available-times") // URL 경로에 designerId 포함
    @ResponseBody
    public AvailableTimeSlotDto getAvailableTimesByPath(
            @PathVariable Long designerId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return reservationService.getAvailableTimeSlots(designerId, date);
    }

    // 예약 작성 Post -> 예약 내역 페이지로
    @PostMapping("/new")
    public String confirmReservation(@ModelAttribute ReservationRequestDto requestDto, @AuthenticationPrincipal CustomUserDetails userDetails){

        // 알림용 아이디  반환
        Long targetId = reservationService.saveReservation(requestDto);

        Long receiverId = userDetails.getId();

        ShopDesigner shopDesigner = shopDesignerRepo.getReferenceById(requestDto.getShopDesignerId());

        Long designerId = shopDesigner.getDesigner().getMember().getId();

        webNotificationService.notify(
                receiverId,
                "예약이 완료되었습니다.",
                WebTarget.RESER_USER,
                targetId
        );

        LocalDateTime reservationDateTime = requestDto.getDateTime();

        String dateStr = reservationDateTime != null
                ? reservationDateTime.toLocalDate().toString()
                : "";  // 또는 null, 혹은 skip할 수도 있음

        webNotificationService.notify(
                designerId,
                "새로운 예약이 생겼습니다",
                WebTarget.RESER_DES,
                targetId
        );

        return "redirect:/myPage/reservation";
    }


    //예약 취소
    @PostMapping("/cancel/{reservationId}")
    @ResponseBody
    public ResponseEntity<?> cancelReservation(@PathVariable Long reservationId,
                                               @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            reservationService.cancelReservation(reservationId, userDetails.getId());
            return ResponseEntity.ok().body(Map.of("result", "success"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }




//    // 예약 확인 페이지 Get
//    @PostMapping("/confirm")
//    public String confirmReservationPage(@ModelAttribute ReservationRequestDto requestDto,Principal principal ,Model model){
//
//        // 예약자 정보 조회
//        String loginId = principal.getName();
//        Member member = memberRepo.findByLoginId(loginId);
//        model.addAttribute("member", member);
//
//
//        // 예약 정보 요약 (디자이너, 시술, 날짜 등)
//        ReservationPreviewDto preview = reservationService.getReservationPreview(requestDto.getMemberId(),requestDto.getShopDesignerId(),
//                requestDto.getShopServiceId(),requestDto.getDateTime().toLocalDate(), requestDto.getDateTime().toLocalTime());
//
//        // 확인용 금액 정보 dto 생성
//        ReservationCheckDto checkDto = reservationService.bulidReservationCheck(requestDto);
//
//
//        // 사용가능한 쿠폰 / 티켓
//        MemberCouponDto couponAndTicket = reservationService.getAvailableCouponAndTicket(requestDto.getMemberId(),
//                preview.getSelectedDesigner().getShopId());
//
//        model.addAttribute("preview",preview);
//        model.addAttribute("checkDto", checkDto);
//        model.addAttribute("requestDto", requestDto);
//        model.addAttribute("couponDto", couponAndTicket);
//
//        return "shop/reCheckCoupon";
//
//    }

    // 예약 확정 및 저장 (post)
    @PostMapping("/complete")
    public String completeReservation(@ModelAttribute ReservationRequestDto requestDto, RedirectAttributes redirect,Principal principal,Model model){

        // 예약자 정보 조회
        String loginId = principal.getName();
        Member member = memberRepo.findByLoginId(loginId);
        model.addAttribute("member",member); // 예약자 정보


        reservationService.saveReservation(requestDto);


        redirect.addFlashAttribute("message", "예약이 성공적으로 완료 되었습니다");
        return "redirect:/shop/complete";
    }

}