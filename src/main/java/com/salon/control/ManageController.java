package com.salon.control;

import com.salon.config.CustomUserDetails;
import com.salon.dto.management.*;
import com.salon.dto.shop.ShopServiceDto;
import com.salon.entity.Member;
import com.salon.entity.management.ShopDesigner;
import com.salon.entity.management.master.Attendance;
import com.salon.entity.shop.Reservation;
import com.salon.repository.management.ShopDesignerRepo;
import com.salon.repository.management.master.AttendanceRepo;
import com.salon.repository.management.master.TicketRepo;
import com.salon.service.management.ManageService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Controller
@AllArgsConstructor
@RequestMapping("/manage")
public class ManageController {

    private final ManageService manageService;
    private final AttendanceRepo attendanceRepo;
    private final ShopDesignerRepo shopDesignerRepo;
    private final TicketRepo ticketRepo;

    // 메인페이지
    @GetMapping("")
    public String getMain(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {

        Long memberId = userDetails.getMember().getId();
        DesignerMainPageDto dto = manageService.getMainPage(memberId);

        model.addAttribute("dto", dto);

        return "management/main";
    }

    // 근태 관리
    @GetMapping("/attendance")
    public String attendance(@AuthenticationPrincipal CustomUserDetails userDetails,
                             @RequestParam(defaultValue = "#{T(java.time.LocalDate).now().withDayOfMonth(1)}") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate monthStart,
                             @RequestParam(required = false) Integer selectedWeek,
                             Model model) {

        ShopDesigner designer = shopDesignerRepo.findByDesigner_Member_IdAndIsActiveTrue(userDetails.getMember().getId());

        LocalDate startOfMonth = monthStart.withDayOfMonth(1);
        LocalDate endOfMonth = monthStart.withDayOfMonth(monthStart.lengthOfMonth());
        LocalDate today = LocalDate.now();

        List<WeekDto> weeks = manageService.getWeeksOfMonth(startOfMonth);
        int defaultWeekIndex = 0;


        for (int i = 0; i < weeks.size(); i++) {
            WeekDto week = weeks.get(i);
            if (!today.isBefore(week.getStartDate()) && !today.isAfter(week.getEndDate())) {
                defaultWeekIndex = i;
                break;
            }
        }

        int currentWeekIndex = selectedWeek != null ? selectedWeek : defaultWeekIndex;

        WeekDto selectedWeekDto = weeks.get(currentWeekIndex);

        List<Attendance> attendanceList = attendanceRepo.findByShopDesignerIdAndClockInBetweenOrderByIdDesc(
                designer.getId(), selectedWeekDto.getStartDate().atStartOfDay(), selectedWeekDto.getEndDate().atTime(LocalTime.MAX));

        List<AttendanceListDto> dtoList = new ArrayList<>();
        for (Attendance att : attendanceList) {
            dtoList.add(AttendanceListDto.from(att));
        }


        model.addAttribute("weeks", weeks);
        model.addAttribute("selectedWeekIndex", currentWeekIndex);
        model.addAttribute("monthStart", startOfMonth);
        model.addAttribute("monthStartStr", startOfMonth.format(DateTimeFormatter.ISO_LOCAL_DATE)); // 날짜 표시용
        model.addAttribute("attendanceList", dtoList);


        return "management/attendance";
    }

    // 출퇴근용 json
    @PostMapping("/attendance/{type}")
    public ResponseEntity<?> saveAttendance(
            @RequestBody String isoTime,
            @PathVariable("type") String type,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Long memberId = userDetails.getMember().getId();
        String trimmed = isoTime.replace("\"", "");
        LocalDateTime time = OffsetDateTime.parse(trimmed).toLocalDateTime();

        if ("start".equalsIgnoreCase(type)) {
            manageService.clockIn(memberId, time);
        } else if ("end".equalsIgnoreCase(type)) {
            manageService.clockOut(memberId, time);
        } else {
            return ResponseEntity.badRequest().body("Invalid attendance type");
        }

        return ResponseEntity.ok().build();
    }

    // 페이지 초기화 시 출퇴근 상태 확인용 API
    @GetMapping("/attendance/status")
    public ResponseEntity<?> getTodayAttendanceStatus(@AuthenticationPrincipal CustomUserDetails userDetails) {
        Long memberId = userDetails.getMember().getId();
        AttendanceStatusDto status = manageService.getTodayStatus(memberId);
        return ResponseEntity.ok(status);
    }

    // 매출 내역
    @GetMapping("/sales")
    public String sales(@AuthenticationPrincipal CustomUserDetails userDetails,
                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                        Model model) {

        // 선택한 날 가져오기
        LocalDate selectedDate = (date != null) ? date : LocalDate.now();

        // 날짜 3일 범위 생성
        List<LocalDate> dateList = new ArrayList<>();
        for (int i = -3; i <= 3; i++) {
            dateList.add(selectedDate.plusDays(i));
        }

        List<PaymentListDto> paymentListDtoList = manageService
                .getPaymentList(userDetails.getMember().getId(), selectedDate);

        System.out.println("selectedDate: " + selectedDate);


        model.addAttribute("dateList", dateList);
        model.addAttribute("selectedDate", selectedDate);
        model.addAttribute("paymentList", paymentListDtoList);

        return "management/sales";
    }

    // 방문 결제 등록 페이지
    @GetMapping("/sales/new")
    public String newSales(Model model,
                           @RequestParam(value = "reservationId", required = false) Long reservationId) {

        PaymentForm paymentForm = manageService.getPaymentForm(reservationId);

        model.addAttribute("newPay", paymentForm);

        return "management/paymentForm";
    }

    // 방문 결제 등록
    @PostMapping("/sales/new")
    public String saveSale(@Valid PaymentForm form,
                           @AuthenticationPrincipal CustomUserDetails userDetails) {

        manageService.savePayment(form, userDetails.getMember().getId());

        return "redirect:/manage/sales";
    }

    // 결제 상세페이지
    @GetMapping("/sales/detail")
    public String salesDetail() {

        return "management/paymentDetail";
    }

    // 예약 내역
    @GetMapping("/reservations")
    public String reservation(@AuthenticationPrincipal CustomUserDetails userDetails,
                              @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                              Model model) {

        // 선택한 날 가져오기
        LocalDate selectedDate = (date != null) ? date : LocalDate.now();

        // 날짜 3일 범위 생성
        List<LocalDate> dateList = new ArrayList<>();
        for (int i = -3; i <= 3; i++) {
            dateList.add(selectedDate.plusDays(i));
        }

        List<ReservationListDto> reservationList = manageService.getReservationList(userDetails.getMember().getId(), selectedDate);

        model.addAttribute("dateList", dateList);
        model.addAttribute("selectedDate", selectedDate);
        model.addAttribute("reservationList", reservationList);

        return "management/reservations";
    }

    // 예약 등록 페이지
    @GetMapping("/reservations/new")
    public String newRes(Model model) {

        model.addAttribute("newRes", new ReservationForm());

        return "management/reservationForm";
    }

    // 예약 수정 페이지
    @GetMapping("/reservations/edit/{id}")
    public String editReservationPage(@PathVariable("id") Long reservationId, Model model) {
        ReservationForm reservationForm = manageService.getReservationForm(reservationId); // 서비스에서 ReservationForm DTO를 가져오는 메서드 필요
        model.addAttribute("newRes", reservationForm); // 기존 등록 폼의 th:object 이름과 동일하게
        model.addAttribute("reservationId", reservationId); // HTML에서 등록/수정 모드를 구분하기 위해 ID 전달
        return "management/reservationForm"; // 기존 등록 폼 템플릿 재사용

    }

    // 예약 수정 Form api
    @GetMapping("/reservations/{id}")
    @ResponseBody // JSON 응답을 위해
    public ResponseEntity<ReservationForm> getReservationDetailsApi(@PathVariable("id") Long id) {
        ReservationForm reservationForm = manageService.getReservationForm(id);
        if (reservationForm == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(reservationForm);
    }

    // 예약 수정
    @PostMapping("/reservations/update/{id}")
    public String updateReservation(@PathVariable("id") Long id,
                                    @AuthenticationPrincipal CustomUserDetails userDetails,
                                    @ModelAttribute ReservationForm reservationForm,
                                    RedirectAttributes redirectAttributes) {
        // reservationForm.setId(id); // PathVariable의 ID를 DTO에 설정 (필요한 경우)
        manageService.saveReservation(reservationForm, userDetails.getId()); // 서비스에서 예약 수정 로직 호출
        redirectAttributes.addFlashAttribute("message", "예약이 성공적으로 수정되었습니다.");
        return "redirect:/manage/reservations"; // 수정 후 예약 목록 페이지로 리다이렉트
    }

    // 시술 검색 api
    @GetMapping("/services/search")
    @ResponseBody
    public ResponseEntity<List<ShopServiceDto>> searchServices(@RequestParam String keyword) {
        List<ShopServiceDto> services = manageService.searchServicesByKeyword(keyword); // 서비스에서 시술 검색 로직 호출
        return ResponseEntity.ok(services);
    }

    // 멤버 검색 시 api
    @ResponseBody
    @GetMapping("/members/search")
    public List<MemberSearchDto> searchMembers(@RequestParam String keyword) {
        return manageService.searchByNameOrPhone(keyword);
    }

    // 쿠폰 조회용 api
    @ResponseBody
    @GetMapping("/members/{memberId}/coupons")
    public MemberCouponDto getMemberCoupons(@PathVariable Long memberId, @AuthenticationPrincipal CustomUserDetails userDetails) {

        Member designer = userDetails.getMember();

        return manageService.getCoupons(memberId, designer.getId());
    }


    // 예약 저장
    @PostMapping("/reservations/save")
    public String saveRes(@Valid ReservationForm newRes, @AuthenticationPrincipal CustomUserDetails userDetails) {

        manageService.saveReservation(newRes, userDetails.getMember().getId());

        return "redirect:/manage/reservations";
    }

    // 회원관리카드
    @GetMapping("/member-card")
    public String memberCard(Model model) {

        return "management/memberCard";
    }

    // 회원관리카드 목록 api
    @GetMapping("/members")
    public ResponseEntity<List<MemberListDto>> getMemberList(@AuthenticationPrincipal CustomUserDetails userDetails) {

        List<MemberListDto> memberList = manageService.getMemberCardList(userDetails.getId());

        return new ResponseEntity<>(memberList, HttpStatus.OK);
    }

    // 회원 개인메모 작성
    @PutMapping("/members/{memberId}/personal-memo")
    public ResponseEntity<Void> updateMemberMemo(
            @PathVariable Long memberId,
            @RequestParam("personal-memo") String newMemoContent,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails.getId() == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        try {
            manageService.writeMemberMemo(memberId, userDetails.getId(), newMemoContent);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 회원 관리카드 메모
    @PutMapping("/members/{paymentId}/card-memo")
    public ResponseEntity<Void> updateMemberCardMemo(
            @PathVariable Long paymentId,
            @RequestParam("card-memo") String newMemoContent) {
        try {
            manageService.writeMemberCardMemo(paymentId, newMemoContent);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }





}
