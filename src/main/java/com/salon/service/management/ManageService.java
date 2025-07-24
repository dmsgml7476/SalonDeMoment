package com.salon.service.management;

import com.salon.constant.AttendanceStatus;
import com.salon.constant.LeaveStatus;
import com.salon.dto.management.WeekDto;
import com.salon.dto.management.*;
import com.salon.dto.management.master.CouponDto;
import com.salon.dto.shop.ShopServiceDto;
import com.salon.entity.Member;
import com.salon.entity.management.*;
import com.salon.entity.management.master.Attendance;
import com.salon.entity.management.master.Coupon;
import com.salon.entity.management.master.ShopService;
import com.salon.entity.management.master.Ticket;
import com.salon.entity.shop.Reservation;
import com.salon.entity.shop.Shop;
import com.salon.repository.MemberRepo;
import com.salon.repository.management.*;
import com.salon.repository.management.master.*;
import com.salon.repository.shop.ReservationRepo;
import com.salon.repository.shop.ShopRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ManageService {

    private final AttendanceRepo attendanceRepo;
    private final DesignerServiceRepo designerServiceRepo;
    private final ShopServiceRepo shopServiceRepo;
    private final LeaveRequestRepo leaveRequestRepo;
    private final PaymentRepo paymentRepo;
    private final ShopDesignerRepo shopDesignerRepo;
    private final ReservationRepo reservationRepo;
    private final ShopRepo shopRepo;
    private final MemberRepo memberRepo;
    private final MemberCouponRepo memberCouponRepo;
    private final TicketRepo ticketRepo;
    private final CouponRepo couponRepo;
    private final MemberMemoRepo memberMemoRepo;
    private final MemberCardRepo memberCardRepo;


    // 메인페이지 출력용 메서드
    public DesignerMainPageDto getMainPage(Long memberId){

        ShopDesigner designer = shopDesignerRepo.findByDesigner_Member_IdAndIsActiveTrue(memberId);

        DesignerMainPageDto dto = new DesignerMainPageDto();

        dto.setTodayReservationCount(reservationRepo.countTodayReservations(designer.getId()));
        dto.setTodayCompletedPayments(paymentRepo.countTodayCompletePayments(designer.getId()) );
        dto.setTodayNewCustomers(reservationRepo.countTodayNewCustomers(designer.getId()));

        List<Reservation> todayRes = reservationRepo.findTodayReservations(designer.getId());

        List<TodayScheduleDto> dtoList = new ArrayList<>();
        for(Reservation res : todayRes) {
            dtoList.add(TodayScheduleDto.from(res));
        }

        dto.setTodayScheduleList(dtoList);

        return dto;
    }

    // 디자이너 출근시 메서드
    @Transactional
    public void clockIn(Long memberId, LocalDateTime clockInTime) {

        ShopDesigner designer = shopDesignerRepo.findByDesigner_Member_IdAndIsActiveTrue(memberId);
        LocalDateTime start = clockInTime.toLocalDate().atStartOfDay();
        LocalDateTime end = clockInTime.toLocalDate().atTime(LocalTime.MAX);


        // 오늘 이미 출근 기록이 있으면 예외 혹은 무시 처리 가능
        boolean exists = attendanceRepo.existsByShopDesignerIdAndClockInBetween(designer.getId(), start, end);
        if (exists) {
            throw new IllegalStateException("이미 오늘 출근 기록이 존재합니다.");
        }

        LocalTime officialStart = designer.getShop().getOpenTime();
        int lateMin = designer.getShop().getLateMin();
        LocalTime clockIn = clockInTime.toLocalTime();

        Attendance attendance = new Attendance();
        attendance.setShopDesigner(designer);
        attendance.setClockIn(clockInTime);
        // 출근 지각 여부
        if (clockIn.isAfter(officialStart.plusMinutes(lateMin))) {
            attendance.setStatus(AttendanceStatus.LATE);
        } else {
            attendance.setStatus(AttendanceStatus.PRESENT);
        }

        attendanceRepo.save(attendance);
    }

    // 디자이너 퇴근시 메서드
    @Transactional
    public void clockOut(Long memberId, LocalDateTime clockOutTime) {
        ShopDesigner designer = shopDesignerRepo.findByDesigner_Member_IdAndIsActiveTrue(memberId);

        LocalDateTime start = clockOutTime.toLocalDate().atStartOfDay();
        LocalDateTime end = clockOutTime.toLocalDate().atTime(LocalTime.MAX);

        // 오늘 출근 기록 조회
        Attendance attendance = attendanceRepo.findByShopDesignerIdAndClockInBetween(designer.getId(), start, end).orElseThrow(
                () -> new IllegalArgumentException("디자이너 못찾음"));

        attendance.setClockOut(clockOutTime);

        // 지각, 조퇴 계산 (예시)
        Shop shop = designer.getShop();

        LocalTime officialStart = shop.getOpenTime();
        LocalTime officialEnd = shop.getCloseTime();

        int earlyLeaveMin = shop.getEarlyLeaveMin();
        LocalTime clockOutLocalTime = clockOutTime.toLocalTime();


        // 퇴근 조퇴 여부
        if (clockOutLocalTime.isBefore(officialEnd.minusMinutes(earlyLeaveMin))) {
            attendance.setStatus(AttendanceStatus.LEFT_EARLY);
        } else {
            attendance.setStatus(AttendanceStatus.LEFT);
        }

        attendanceRepo.save(attendance);
    }

    // 오늘 출퇴근 시간 출력용
    public AttendanceStatusDto getTodayStatus(Long memberId){

        ShopDesigner designer = shopDesignerRepo.findByDesigner_Member_IdAndIsActiveTrue(memberId);

        LocalDate today = LocalDate.now();
        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end = today.atTime(LocalTime.MAX);


        Optional<Attendance> attendanceOpt = attendanceRepo.findByShopDesignerIdAndClockInBetween(designer.getId(), start, end);

        if (attendanceOpt.isEmpty()) {
            return new AttendanceStatusDto(false, null, null);
        }

        Attendance attendance = attendanceOpt.get();

        String clockInStr = attendance.getClockIn() == null ? null :
                attendance.getClockIn().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));

        String clockOutStr = attendance.getClockOut() == null ? null :
                attendance.getClockOut().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));

        boolean isWorking = attendance.getClockOut() == null;

        return new AttendanceStatusDto(isWorking, clockInStr, clockOutStr);

    }

    // 디자이너 휴가 신청 시
    @Transactional
    public void saveLeaveRequest(LeaveRequestDto dto, Long memberId){

        ShopDesigner designer = shopDesignerRepo.findByDesigner_Member_IdAndIsActiveTrue(memberId);

        dto.setStatus(LeaveStatus.REQUESTED);
        LeaveRequest leaveRequest = dto.to(designer);

        leaveRequestRepo.save(leaveRequest);

    }
    
//    // 디자이너 개인 출퇴근 목록
//    public List<AttendanceListDto> getAttendanceList(Long memberId, LocalDate selectedDate){
//
//        ShopDesigner designer = shopDesignerRepo.findByDesigner_Member_IdAndIsActiveTrue(memberId);
//
//        // selectedDate 00 : 00
//        LocalDateTime start = selectedDate.atStartOfDay();
//        // selectedDate 23 : 59 : 59.999
//        LocalDateTime end = selectedDate.atTime(LocalTime.MAX);
//        List<Attendance> attendanceList = attendanceRepo.findByShopDesignerIdAndClockInBetweenOrderByIdDesc(designer.getId(), start, end);
//
//        List<AttendanceListDto> dtoList = new ArrayList<>();
//
//        for(Attendance att : attendanceList){
//            dtoList.add(AttendanceListDto.from(att));
//        }
//
//        return dtoList;
//    }

    // 근태 목록 페이지 주간 목록 생성
    public List<WeekDto> getWeeksOfMonth(LocalDate monthStart){

        List<WeekDto> weeks = new ArrayList<>();
        LocalDate firstDayOfMonth = monthStart.withDayOfMonth(1);
        LocalDate lastDayOfMonth = monthStart.withDayOfMonth(monthStart.lengthOfMonth());

        LocalDate date = firstDayOfMonth.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate endDate = lastDayOfMonth.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));


        while ( !date.isAfter(endDate) ) {
            LocalDate start = date;
            LocalDate end = date.plusDays(6);
            weeks.add(new WeekDto(start, end, getLabel(weeks.size() + 1, start, end)));
            date = date.plusWeeks(1);
        }

        return weeks;
    }

    // 위의 주간 라벨 표시용
    private String getLabel(int weekNum, LocalDate start, LocalDate end) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM.dd");
        return weekNum + "주차 (" + start.format(fmt) + "~" + end.format(fmt) + ")";
    }

    // 디자이너 개인 예약 현황
    public List<ReservationListDto> getReservationList(Long memberId, LocalDate selectedDate){

        ShopDesigner designer = shopDesignerRepo.findByDesigner_Member_IdAndIsActiveTrue(memberId);
        LocalDateTime start = selectedDate.atStartOfDay();
        LocalDateTime end = selectedDate.atTime(LocalTime.MAX);

        List<Reservation> reservationListlist =
                reservationRepo.findByShopDesignerIdAndReservationDateBetweenOrderByReservationDateDesc(designer.getId(), start, end);

        List<ReservationListDto> dtoList = new ArrayList<>();
        for(Reservation entity : reservationListlist) {

            ReservationListDto dto = ReservationListDto.from(entity);
            // 당일 예약 구분
            dto.setToday(entity.getReservationDate().toLocalDate().isEqual(LocalDate.now()));

            // 해당 예약 결제 여부
            boolean isPaid = paymentRepo.existsByReservation(entity);
            dto.setPaid(isPaid);

            dtoList.add(dto);
        }

        return dtoList;
    }

    // 예약 등록 시 멤버 검색 api
    public List<MemberSearchDto> searchByNameOrPhone(String keyword) {
        List<Member> members = memberRepo.findByNameContainingIgnoreCaseOrTelContaining(keyword, keyword);
        return members.stream()
                .map(m -> new MemberSearchDto(m.getId(), m.getName(), m.getTel()))
                .collect(Collectors.toList());
    }

    public MemberCouponDto getCoupons(Long memberId, Long designerId) {
        // 1. 쿠폰 조회
        List<MemberCoupon> coupons = memberCouponRepo.findAvailableCouponsByMemberId(memberId);

        Long shopId = shopDesignerRepo.findByDesigner_Member_IdAndIsActiveTrue(designerId).getShop().getId();

        List<CouponDto> couponDtos = new ArrayList<>();

        for (MemberCoupon coupon : coupons) {
            CouponDto dto = CouponDto.from(coupon.getCoupon());
            couponDtos.add(dto);
        }

        // 2. 정액권 잔액 계산
        Optional<Ticket> optionalTicket = ticketRepo.findByMemberIdAndShopId(memberId, shopId);
        int ticketBalance = optionalTicket
                .map(ticket -> ticket.getTotalAmount() - ticket.getUsedAmount())
                .orElse(0);

        // 3. 통합 반환
        return new MemberCouponDto(couponDtos, ticketBalance);
    }

    // 예약 수정 Form
    public ReservationForm getReservationForm(Long reservationId) {
        Reservation res = reservationRepo.findById(reservationId).orElseThrow();

        return ReservationForm.from(res);
    }


    // 디자이너 페이지 예약 저장시
    @Transactional
    public void saveReservation(ReservationForm newRes, Long memberId){

        // Member 조회
        Member member = memberRepo.findById(newRes.getMemberId())
                .orElseThrow(() -> new IllegalArgumentException("회원 정보를 찾을 수 없습니다. id=" + newRes.getMemberId()));

        // ShopDesigner 조회 (memberId가 디자이너 멤버 아이디라고 가정)
        ShopDesigner shopDesigner = shopDesignerRepo.findByDesigner_Member_IdAndIsActiveTrue(memberId);
        if (shopDesigner == null) {
            throw new IllegalArgumentException("활성화된 디자이너를 찾을 수 없습니다. memberId=" + memberId);
        }

        // ShopService 조회
        ShopService shopService = null;
        if (newRes.getServiceId() != null && newRes.getServiceId() != 0) {
            shopService = shopServiceRepo.findById(newRes.getServiceId())
                    .orElseThrow(() -> new IllegalArgumentException("서비스 정보를 찾을 수 없습니다. id=" + newRes.getServiceId()));
        }

        // Coupon 조회 (선택된 경우)
        Coupon coupon = null;
        if (newRes.getCouponId() != null && newRes.getCouponId() != 0) {
            coupon = couponRepo.findById(newRes.getCouponId())
                    .orElseThrow(() -> new IllegalArgumentException("쿠폰 정보를 찾을 수 없습니다. id=" + newRes.getCouponId()));
        }

        // Ticket 조회 (정액권 사용 시)
        Ticket ticket = null;
        if (newRes.isTicketIsUsed() && newRes.getTicketUsedAmount() > 0) {
            System.out.println("shopId = " + shopDesigner.getShop());
            System.out.println("shopId = " + (shopDesigner.getShop() != null ? shopDesigner.getShop().getId() : "null"));
            ticket = ticketRepo.findByMemberIdAndShopId(member.getId(), shopDesigner.getShop().getId())
                    .orElse(null);
        }

        Reservation reservation;

        // 수정인지 신규 저장인지 판단
        if (newRes.getId() != null) {
            reservation = reservationRepo.findById(newRes.getId())
                    .orElseThrow(() -> new IllegalArgumentException("예약을 찾을 수 없습니다. id=" + newRes.getId()));

            // 수정 시에는 기존 entity에 값을 세팅하거나 새로 만들어서 교체 가능
            // 여기서는 새로 만든 객체로 교체하지 않고 기존 엔티티에 값을 세팅하는 방식을 추천
            reservation.setMember(member);
            reservation.setShopDesigner(shopDesigner);
            reservation.setShopService(shopService);
            reservation.setCoupon(coupon);
            reservation.setTicket(ticket);
            reservation.setDiscountAmount(newRes.getCouponDiscount());
            reservation.setTicketUsedAmount(newRes.isTicketIsUsed() ? newRes.getTicketUsedAmount() : 0);
            reservation.setReservationDate(newRes.getReservationDate() != null ? newRes.getReservationDate() : reservation.getReservationDate());
            reservation.setStatus(newRes.getStatus() != null ? newRes.getStatus() : reservation.getStatus());
            reservation.setComment(newRes.getComment());
            reservation.setServiceName(shopService != null ? shopService.getName() : newRes.getServiceName());
        } else {
            // 신규 저장 시 DTO의 to() 메서드 활용
            reservation = newRes.to(member, shopDesigner, shopService, coupon, ticket);
        }

        reservationRepo.save(reservation);

    }

    // 예약 등록시 시술 검색용
    public List<ShopServiceDto> searchServicesByKeyword(String keyword) {
        List<ShopService> services = shopServiceRepo.findByNameContainingIgnoreCase(keyword);

        List<ShopServiceDto> dtoList = new ArrayList<>();
        for(ShopService service : services){
            dtoList.add(ShopServiceDto.from(service));
        }

        return dtoList;
    }

    // 디자이너 매출 목록 (일별)
    public List<PaymentListDto> getPaymentList(Long memberId, LocalDate selectedDate){

        LocalDateTime start = selectedDate.atStartOfDay();
        LocalDateTime end = selectedDate.atTime(LocalTime.MAX);
        ShopDesigner designer = shopDesignerRepo.findByDesigner_Member_IdAndIsActiveTrue(memberId);

        List<Payment> paymentList = paymentRepo.findByDesignerAndPeriod(designer.getId(), start, end);

        System.out.println(paymentList);

        List<PaymentListDto> dtoList = new ArrayList<>();
        for(Payment payment : paymentList) {
            dtoList.add(PaymentListDto.from(payment));
        }

        return dtoList;
    }

    // 예약 결제등록시
    public PaymentForm getPaymentForm(Long reservationId) {

        PaymentForm form = new PaymentForm();

        form.setPayDate(LocalDateTime.now());

        if (reservationId != null) {
            Reservation reservation = reservationRepo.findById(reservationId).orElse(null);
            if (reservation != null) {

                form.setReservationId(reservation.getId());
                form.setCustomerName(reservation.getMember().getName());
                form.setCustomerTel(reservation.getMember().getTel());
                form.setServiceName(reservation.getShopService().getName());
                form.setDesignerId(reservation.getShopDesigner().getId());
                form.setTotalPrice(reservation.getShopService().getPrice());
                form.setServiceCategory(reservation.getShopService().getCategory());
                form.setCouponDiscountPrice(reservation.getDiscountAmount());
                form.setTicketUsedPrice(reservation.getTicketUsedAmount());

            } else {
                System.err.println("경고: 예약 ID " + reservationId + "에 해당하는 예약을 찾을 수 없습니다.");
            }
        }
        return form;
    }
    
    // 결제내역 등록
    @Transactional
    public void savePayment(PaymentForm form, Long memberId){

        Reservation reservation = null;
        if(form.getReservationId() != null){
            reservation = reservationRepo.findById(form.getReservationId())
                    .orElseThrow(() -> new IllegalArgumentException("예약 ID가 유효하지 않습니다."));
        }
        ShopDesigner designer = shopDesignerRepo.findByDesigner_Member_IdAndIsActiveTrue(memberId);

        Payment payment = form.to(reservation, designer);

        paymentRepo.save(payment);
    }

    // 회원 관리카드목록
    public List<MemberListDto> getMemberCardList(Long designerId) {

        ShopDesigner designer = shopDesignerRepo.findByDesigner_Member_IdAndIsActiveTrue(designerId);

        // 디자이너에게 시술받은 회원 목록
        List<Member> members = paymentRepo.findDistinctMembersByShopDesignerId(designer.getId());

        List<MemberListDto> memberList = new ArrayList<>();
        for(Member member : members){
            // 회원 메모
            MemberMemo memberMemo = memberMemoRepo.findByMemberIdAndShopDesignerId(member.getId(), designer.getId())
                    .orElse(null);

            // 해당 회원 결제내역(방문x)
            List<Payment> payments = paymentRepo.findByMemberIdAndDesignerId(member.getId(), designer.getId());
            List<MemberCardListDto> cardList = new ArrayList<>();
            for(Payment payment : payments){
                MemberCard memberCard = memberCardRepo.findByPaymentId(payment.getId()).orElse(null);
                cardList.add(MemberCardListDto.from(memberCard, payment));
            }

            memberList.add(MemberListDto.from(member, memberMemo, cardList));
        }

        return memberList;
    }

    // 회원 개인메모 작성
    @Transactional
    public void writeMemberMemo(Long memberId, Long designerId, String newMemoContent) {

        // 디자이너 ShopDesigner 객체
        ShopDesigner designer = shopDesignerRepo.findByDesigner_Member_IdAndIsActiveTrue(designerId);

        MemberMemo memberMemo = memberMemoRepo.findByMemberIdAndShopDesignerId(memberId, designer.getId()).orElse(null);
        if(memberMemo != null){
            memberMemo.setMemo(newMemoContent);
            memberMemoRepo.save(memberMemo);
        } else{
            MemberMemo memo = new MemberMemo();
            Member member = memberRepo.findById(memberId).orElseThrow();
            memo.setMember(member);
            memo.setShopDesigner(designer);
            memo.setMemo(newMemoContent);
            memberMemoRepo.save(memo);
        }

    }

    // 회원 관리카드 작성
    @Transactional
    public void writeMemberCardMemo(Long paymentId, String newMemoContent) {

        Payment payment = paymentRepo.findById(paymentId).orElseThrow();

        MemberCard memberCard = memberCardRepo.findByPaymentId(paymentId).orElse(null);
        if(memberCard != null) {
            memberCard.setMemo(newMemoContent);
            memberCardRepo.save(memberCard);
        } else {
            MemberCard newCard = new MemberCard();
            newCard.setPayment(payment);
            newCard.setMemo(newMemoContent);
            newCard.setCreateAt(LocalDateTime.now());
            memberCardRepo.save(newCard);
        }
    }

}

