package com.salon.service.shop;


import com.salon.constant.LikeType;
import com.salon.constant.ReservationStatus;
import com.salon.constant.ServiceCategory;
import com.salon.dto.designer.DesignerListDto;
import com.salon.dto.management.MemberCouponDto;
import com.salon.dto.management.master.CouponDto;
import com.salon.dto.management.master.TicketInfoDto;
import com.salon.dto.shop.*;
import com.salon.dto.user.MemberDto;
import com.salon.dto.user.ReReservationFormDto;
import com.salon.entity.Member;
import com.salon.entity.management.MemberCoupon;
import com.salon.entity.management.ShopDesigner;
import com.salon.entity.management.master.Coupon;
import com.salon.entity.management.master.DesignerService;
import com.salon.entity.management.master.ShopService;
import com.salon.entity.management.master.Ticket;
import com.salon.entity.shop.Reservation;
import com.salon.entity.shop.Shop;
import com.salon.repository.MemberRepo;
import com.salon.repository.ReviewRepo;
import com.salon.repository.management.MemberCouponRepo;
import com.salon.repository.management.ShopDesignerRepo;
import com.salon.repository.management.master.*;
import com.salon.repository.shop.ReservationRepo;
import com.salon.repository.shop.SalonLikeRepo;
import com.salon.util.DayOffUtil;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.AccessDeniedException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ShopServiceRepo shopServiceRepo;
    private final ShopDesignerRepo shopDesignerRepo;
    private final DesignerServiceRepo designerServiceRepo;
    private final SalonLikeRepo salonLikeRepo;
    private final ReviewRepo reviewRepo;
    private final ReservationRepo reservationRepo;
    private final MemberRepo memberRepo;
    private final MemberCouponRepo memberCouponRepo;
    private final TicketRepo ticketRepo;
    private final TicketUsageRepo ticketUsageRepo;
    private final CouponRepo couponRepo;

    // 디자이너 리스트 섹션
    public List<DesignerListDto> getDesignerList(Long shopId){
        List<ShopDesigner> designerList = shopDesignerRepo.findByShopIdAndIsActiveTrue(shopId);
        List<DesignerListDto> dtos = new ArrayList<>();

        for (ShopDesigner sd : designerList) {
            Long designerId = sd.getId();

            // 디자이너 찜 갯수 조회
            int likeCount = salonLikeRepo.countByLikeTypeAndTypeId(LikeType.DESIGNER, designerId);

            // 디자이너 리뷰 갯수 조회
            int reviewcount = reviewRepo.countByReservation_ShopDesigner_Id(designerId);

            // 디자이너 시술 카테고리 조회
            DesignerService designerService = designerServiceRepo.findByShopDesignerId(designerId).orElse(null);

            // dto로 반환
            DesignerListDto designerListDto = DesignerListDto.from(sd,likeCount,reviewcount, designerService);
            dtos.add(designerListDto);
        }
        return dtos;
    }

    // 매장 전체 시술 , 카테고리 리스트
    public List<DesignerServiceCategoryDto> getAllServiceCategories(Long shopId) {
        List<ShopService> allShopServices = shopServiceRepo.findByShopId(shopId);

        Set<ServiceCategory> allCategoriesInShop = new HashSet<>();
        for (ShopService service : allShopServices) {
            allCategoriesInShop.add(service.getCategory());
        }

        Map<ServiceCategory, List<ShopServiceDto>> categoryMap = new HashMap<>();
        for (ShopService shopService : allShopServices) {
            ServiceCategory category = shopService.getCategory();
            categoryMap.computeIfAbsent(category, k -> new ArrayList<>()).add(ShopServiceDto.from(shopService));
        }

            List<DesignerServiceCategoryDto> dtos = new ArrayList<>();
            List<ServiceCategory> sortedCategories = new ArrayList<>(allCategoriesInShop);

            for (ServiceCategory cate : sortedCategories) {
                DesignerServiceCategoryDto dto = new DesignerServiceCategoryDto();
                dto.setCategory(cate);
                // 맵에서 해당 카테고리에 해당하는 시술 리스트(ShopServiceDto 리스트)를 가져오고, 없으면 빈 리스트를 반환
                dto.setServices(categoryMap.getOrDefault(cate, new ArrayList<>()));
                dtos.add(dto);
            }

            // 5. 최종 DTO 리스트 반환
            return dtos;

    }


    // 선택한 디자이너 전문 시술 분야 카테고리 및 시술 리스트
    public List<DesignerServiceCategoryDto> getDesignerServiceCategories(Long shopDesignerId) {

        // 디자이너의 서비스 구성 정보 조회
        DesignerService designerService = designerServiceRepo.findByShopDesignerId(shopDesignerId)
                .orElseThrow(() -> new EntityNotFoundException("선택한 디자이너의 해당하는 서비스가 없습니다"));

        // 담당 카테고리 추출
        List<ServiceCategory> assignedCategories = designerService.getAssignedCategories();

        // 소속 매장 id 조회
        Long shopId = designerService.getShopDesigner().getShop().getId();

        // 해당 매장의 시술 리스트 중 디자이너가 담당하는 카테고리만 필터링
        List<ShopService> matchedServices = shopServiceRepo.findByShopIdAndCategoryIn(shopId,assignedCategories);

        // 카테고리별 분류
        Map<ServiceCategory, List<ShopServiceDto>> categoryMap = new HashMap<>();
        for (ShopService shopService : matchedServices) {
            ServiceCategory category = shopService.getCategory();
            categoryMap.computeIfAbsent(category, k -> new ArrayList<>()).add(ShopServiceDto.from(shopService));
        }

        // dto 변환
        List<DesignerServiceCategoryDto> dtos = new ArrayList<>();
        for (ServiceCategory category : assignedCategories) {
            DesignerServiceCategoryDto dto = new DesignerServiceCategoryDto();
            dto.setCategory(category);
            dto.setServices(categoryMap.getOrDefault(category, new ArrayList<>()));
            dtos.add(dto);
        }

        return dtos;

    }


    // 날짜 시간 선택 섹션
    public AvailableTimeSlotDto getAvailableTimeSlots (Long shopDesignerId, LocalDate date) {

        if (shopDesignerId == null) {
            throw new IllegalArgumentException("디자이너 ID가 전달되지 않았습니다.");
        }
        ShopDesigner designer = shopDesignerRepo.findByIdAndIsActiveTrue(shopDesignerId);
        if (designer == null) {
            throw new IllegalArgumentException("해당 디자이너가 존재하지 않거나 비활성화 상태입니다.");
        }


        ShopDesigner designerActive = shopDesignerRepo.findByIdAndIsActiveTrue(shopDesignerId);
        Shop shop = designer.getShop();

        LocalTime startTime = designer.getScheduledStartTime() != null ? designer.getScheduledStartTime() : shop.getOpenTime();
        LocalTime endTime = designer.getScheduledEndTime() != null ? designer.getScheduledEndTime() : shop.getCloseTime();

        // 휴무일 계산
        List<DayOfWeek> shopDayOff = DayOffUtil.decodeDayOff(shop.getDayOff());
        boolean isHoliday = shopDayOff.contains(date.getDayOfWeek());

        // 이미 예약된 시간
        LocalDateTime daystart = date.atTime(startTime);
        LocalDateTime dayEnd = date.atTime(endTime);
        List<Reservation> existing = reservationRepo.findByShopDesignerIdAndReservationDateBetween(shopDesignerId,daystart,dayEnd);
        Set<LocalTime> reserveTimes = existing.stream()
                .map(r -> r.getReservationDate().toLocalTime())
                .collect(Collectors.toSet());

        // 시간 슬롯 생성
        List<LocalTime> avaiiable = new ArrayList<>();

        // 예약시간 간격 설정
        int interval = shop.getReservationInterval();

        for (LocalTime t = startTime; t.plusMinutes(interval).isBefore(endTime.plusSeconds(1)); t = t.plusMinutes(interval)) {

            if (!reserveTimes.contains(t)){
                avaiiable.add(t);
            }

        }

        // 결과 dto 구성
        AvailableTimeSlotDto dto = new AvailableTimeSlotDto();
        dto.setDate(date);
        dto.setAvailableTimes(isHoliday ? List.of() : avaiiable); // 휴무일이면 빈 리스트
        dto.setHoliday(isHoliday);

        return dto;
    }



    // 예약 확인 메서드
    public ReservationPreviewDto getReservationPreview(Long memberId, Long shopDesignerId,Long shopServiceId, LocalDate date, LocalTime time){

        ShopDesigner designer = shopDesignerRepo.findByIdAndIsActiveTrue(shopDesignerId);
        DesignerListDto selectedDesigner = DesignerListDto.from(designer,salonLikeRepo.countByLikeTypeAndTypeId(LikeType.DESIGNER,shopDesignerId),reviewRepo.countByReservation_ShopDesigner_Id(shopDesignerId), designerServiceRepo.findByShopDesignerId(designer.getId()).orElse(null));

        ShopService service = shopServiceRepo.findById(shopServiceId)
                .orElseThrow(() -> new EntityNotFoundException("시술 정보 없음"));
        ShopServiceDto selectedService = ShopServiceDto.from(service);

        Member member = memberRepo.findById(memberId)
                .orElseThrow(()-> new EntityNotFoundException("회원 정보 없음"));

        ReservationPreviewDto dto = new ReservationPreviewDto();
        dto.setSelectedDesigner(selectedDesigner);
        dto.setSelectedService(selectedService);
        dto.setReservationDate(date);
        dto.setReservationTime(time);
        dto.setTotalPrice(service.getPrice());
        dto.setShopName(designer.getShop().getName());
        dto.setShopAddress(designer.getShop().getAddress());
        dto.setServiceSummary(service.getName() + " - " + service.getPrice() + "원 ");


        //예약자 정보 dto
        MemberDto memberDto = new MemberDto();
        memberDto.setName(member.getName());
        memberDto.setPhone(member.getTel());

        dto.setDesignerTitle(selectedDesigner.getName());

        return dto;


    }

    // 예약 확인페이지에서  고객 요청사항 작성 및 예약시 유의사항 출력
    public ReservationCheckDto bulidReservationCheck(ReservationRequestDto requestDto) {
        Reservation reservation = new Reservation();

        ShopService service = shopServiceRepo.findById(requestDto.getShopServiceId())
                .orElseThrow(() -> new EntityNotFoundException("시술 정보를  찾을  수 없습니다"));

        int serviceAmount = service.getPrice();

        // 할인정보 계산 생략 가능시 (이미 별도 메서드 존재)
        int couponnDiscount = 0;
        int ticketUsedAmount = 0;
        int finalAmount = serviceAmount;

        // 요청 사항 반영
        reservation.setComment(requestDto.getRequestMemo());

        // 금액 정보만 표현
        ReservationCheckDto dto = new ReservationCheckDto();
        dto.setServiceAmount(serviceAmount);
        dto.setAmountDiscount(couponnDiscount);
        dto.setTicketUsedAmount(ticketUsedAmount);
        dto.setFinalAmount(finalAmount);

        // 주의사항 고정 안내
        dto.setPrecaution("예약 10분 전까지 도착 부탁드립니다 :)");

        // 예약 작성 정보 (날짜 시간만 표현)
        dto.setReservationDate(requestDto.getDateTime().toLocalDate());
        dto.setReservationTime(requestDto.getDateTime().toLocalTime());

        return dto;

    }




    // 쿠폰 / 정액권 선택 섹션
    public MemberCouponDto getAvailableCouponAndTicket(Long memberId, Long shopId){

        // 사용자가 보유한 쿠폰 중, 사용 가능 조건에 맞는 것만 필터링
        List<MemberCoupon> ownedCoupons = memberCouponRepo.findAvailableCouponsByMemberId(memberId);

        List<CouponDto> validCoupons = ownedCoupons.stream()
                .filter(mc -> mc.getCoupon().getShop().getId().equals(shopId))
                .map(mc -> CouponDto.from(mc.getCoupon()))
                .collect(Collectors.toList());

        // 해당 매장 소속 정액권 보유 금액 조회
       List<Ticket> tickets = ticketRepo.findByMemberIdOrderByCreateAtDesc(memberId);

       List<TicketInfoDto> dtos = tickets.stream()
               .map(ticket -> {
                   int used = ticketUsageRepo.findTotalUsedAmountByTicketId(ticket.getId());
                   return TicketInfoDto.from(ticket,used);

               }).toList();

       int totalRemainingTicketAmount = dtos.stream()
               .mapToInt(TicketInfoDto::getRemainingAmount)
               .sum();


        // dto반환
        return new MemberCouponDto(validCoupons,totalRemainingTicketAmount);

    }


    // 예약 작성 완료 후 entity에 저장하는 메서드
    @Transactional
    public Long saveReservation(ReservationRequestDto requestDto){

        // 예약자 정보
        Member member = memberRepo.findById(requestDto.getMemberId())
                .orElseThrow(() -> new EntityNotFoundException("회원 정보 없음"));

        // 디자이너 / 시술 정보
        ShopDesigner designer = shopDesignerRepo.getReferenceById(requestDto.getShopDesignerId());
        ShopService service = shopServiceRepo.getReferenceById(requestDto.getShopServiceId());

        // 예약 엔티티 생성
        Reservation reservation = new Reservation();
        reservation.setMember(member);
        reservation.setShopDesigner(designer);
        reservation.setShopService(service);
        reservation.setReservationDate(requestDto.getDateTime());
        reservation.setStatus(ReservationStatus.RESERVED);
        reservation.setServiceName(service.getName());

        // 고객 요청사항
        reservation.setComment(requestDto.getRequestMemo());

        // 쿠폰 적용
        if (requestDto.getSelectCouponId() != null) {
            Coupon coupon = couponRepo.getReferenceById(requestDto.getSelectCouponId());
            reservation.setCoupon(coupon);
        }

        // 정액권 적용
        if (requestDto.getSelectTicketId() != null){
            Ticket ticket = ticketRepo.getReferenceById(requestDto.getSelectTicketId());
            reservation.setTicket(ticket);
        }

        // 저장
        reservationRepo.save(reservation);

        // 웹 알림 저장용 아이디 반환
        return reservation.getId();
    }


    // 예약 취소
    public void cancelReservation(Long reservationId, Long memberId) {
        Reservation reservation = reservationRepo.findByIdAndMemberId(reservationId, memberId).orElseThrow(() ->
                new IllegalArgumentException("예약 없음"));

        if(reservation.getStatus() != ReservationStatus.RESERVED) {
            throw new IllegalArgumentException("이미 취소된 예약입니다.");
        }

        reservation.setStatus(ReservationStatus.CANCELED);

        reservationRepo.save(reservation);

    }

}
