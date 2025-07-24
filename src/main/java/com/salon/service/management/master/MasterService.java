package com.salon.service.management.master;

import com.salon.constant.LeaveStatus;
import com.salon.constant.LikeType;
import com.salon.constant.ServiceCategory;
import com.salon.constant.UploadType;
import com.salon.dto.UploadedFileDto;
import com.salon.dto.designer.DesignerListDto;
import com.salon.dto.management.AttendanceListDto;
import com.salon.dto.management.LeaveRequestDto;
import com.salon.dto.management.ServiceForm;
import com.salon.dto.management.TodayScheduleDto;
import com.salon.dto.management.master.*;
import com.salon.entity.management.Designer;
import com.salon.entity.management.LeaveRequest;
import com.salon.entity.management.Payment;
import com.salon.entity.management.ShopDesigner;
import com.salon.entity.management.master.Attendance;
import com.salon.entity.management.master.Coupon;
import com.salon.entity.management.master.DesignerService;
import com.salon.entity.management.master.ShopService;
import com.salon.entity.shop.Reservation;
import com.salon.entity.shop.Shop;
import com.salon.entity.shop.ShopImage;
import com.salon.repository.MemberRepo;
import com.salon.repository.ReviewRepo;
import com.salon.repository.management.DesignerRepo;
import com.salon.repository.management.LeaveRequestRepo;
import com.salon.repository.management.PaymentRepo;
import com.salon.repository.management.ShopDesignerRepo;
import com.salon.repository.management.master.*;
import com.salon.repository.shop.ReservationRepo;
import com.salon.repository.shop.SalonLikeRepo;
import com.salon.repository.shop.ShopImageRepo;
import com.salon.repository.shop.ShopRepo;
import com.salon.util.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.*;

@Service
@RequiredArgsConstructor
public class MasterService {

    private final ReservationRepo reservationRepo;
    private final AttendanceRepo attendanceRepo;
    private final CouponRepo couponRepo;
    private final DesignerServiceRepo designerServiceRepo;
    private final ShopServiceRepo shopServiceRepo;
    private final ShopClosedDateRepo shopClosedDateRepo;
    private final TicketRepo ticketRepo;
    private final LeaveRequestRepo leaveRequestRepo;
    private final PaymentRepo paymentRepo;
    private final ShopDesignerRepo shopDesignerRepo;
    private final SalonLikeRepo salonLikeRepo;
    private final ReviewRepo reviewRepo;
    private final MemberRepo memberRepo;
    private final DesignerRepo designerRepo;
    private final ShopRepo shopRepo;
    private final ShopImageRepo shopImageRepo;
    private final FileService fileService;

    // 메인페이지용
    public MainDesignerPageDto getMainPage(Long memberId) {

        // 소속 미용실
        ShopDesigner designer = shopDesignerRepo.findByDesigner_Member_IdAndIsActiveTrue(memberId);
        Shop shop = designer.getShop();

        // 미용실 소속 디자이너
        List<ShopDesigner> designers = shopDesignerRepo.findByShopIdAndIsActiveTrue(shop.getId());

        MainDesignerPageDto dto = new MainDesignerPageDto();

        // 소속 디자이너 수
        dto.setDesignerCount(designers.size());

        // 오늘의 예약 수 (미용실)
        int countRes = 0;
        for(ShopDesigner shopDesigner : designers) {
            countRes += reservationRepo.countTodayReservations(shopDesigner.getId());
        }
        dto.setTodayReservationCount(countRes);

        // 오늘 매출
        int todaySumPay = 0;
        for(ShopDesigner shopDesigner : designers){
            todaySumPay += paymentRepo.sumTodayTotalPrice(shopDesigner.getDesigner().getId());
        }
        dto.setTodayPay(String.format("₩%,d", todaySumPay));

        // 월간 매출
        int monthlyPay = paymentRepo.sumMonthlyTotalPrice(shop.getId());
        dto.setMonthlyPay(String.format("₩%,d", monthlyPay));

        // 소속 디자이너 목록 dto
        List<DesignerSummaryDto> designerDtoList = new ArrayList<>();
        for(ShopDesigner shopDesigner : designers ){
            int todayResCount = reservationRepo.countTodayReservations(shopDesigner.getId());
            designerDtoList.add(DesignerSummaryDto.from(shopDesigner, todayResCount));
        }

        dto.setDesignerList(designerDtoList);
        
        // 미용실 예약 목록
        List<Reservation> resList = reservationRepo.findTodayResByShopId(shop.getId());
        List<TodayScheduleDto> todayScheduleDtoList = new ArrayList<>();
        for(Reservation res : resList){
            todayScheduleDtoList.add(TodayScheduleDto.from(res));
        }
        dto.setTodaySchedules(todayScheduleDtoList);

        return dto;
    }


    // 소속 미용실 휴가요청 목록 가져오기
    public List<LeaveRequestDto> getLeaveRequestList(Long shopId){

        List<LeaveRequest> leaveRequestList = leaveRequestRepo.findByShopDesigner_Shop_IdOrderByRequestAtDesc(shopId);
        List<LeaveRequestDto> list = new ArrayList<>();

        for(LeaveRequest request : leaveRequestList){
            list.add(LeaveRequestDto.from(request));
        }

        return list;
    }

    // 디자이너 휴가 상태 변경시
    @Transactional
    public void updateLeaveRequest(Long leaveRequestId, LeaveStatus status){
        LeaveRequest leaveRequest = leaveRequestRepo.findById(leaveRequestId).orElseThrow(()
                -> new IllegalStateException("없는 요청읾"));

        leaveRequest.setStatus(status);
        leaveRequest.setApprovedAt(LocalDateTime.now());

    }

    // 미용실 소속 디자이너 목록 가져오기
    public List<DesignerListDto> getDesignerList(Long memberId){

        Long shopId = shopDesignerRepo.findByDesigner_Member_IdAndIsActiveTrue(memberId).getShop().getId();

        List<ShopDesigner> designerList = shopDesignerRepo.findByShopIdAndIsActiveTrue(shopId);

        List<DesignerListDto> dtoList = new ArrayList<>();
        for(ShopDesigner designer : designerList){
            DesignerService service = designerServiceRepo.findByShopDesignerId(designer.getId()).orElse(null);
            int likeCount = salonLikeRepo.countByLikeTypeAndTypeId(LikeType.DESIGNER, designer.getId());
            int reviewCount = reviewRepo.countByReservation_ShopDesigner_Id(designer.getId());
            dtoList.add(DesignerListDto.from(designer, likeCount, reviewCount, service));
        }

        return dtoList;
    }

    // 디자이너 검색결과 리스트
    public List<DesignerResultDto> getDesignerResult(DesignerSearchDto searchDto) {

        List<Designer> foundDesigners = new ArrayList<>();

        // 검색 조건에 따라 리포지토리 메서드 호출
        if (searchDto.getName() != null && !searchDto.getName().trim().isEmpty()) {
            foundDesigners = designerRepo.findByMember_NameContainingIgnoreCase(searchDto.getName());
        } else if (searchDto.getTel() != null && !searchDto.getTel().trim().isEmpty()) {
            foundDesigners = designerRepo.findByMember_Tel(searchDto.getTel());
        } else {
            // 검색어가 없으면 빈 목록 반환 (또는 모든 활성 디자이너 반환 등 정책에 따라)
            return new ArrayList<>();
        }

        List<DesignerResultDto> dtoList = new ArrayList<>();
        for (Designer designer : foundDesigners) {
            boolean isAffiliated = shopDesignerRepo.existsByDesigner_Id(designer.getId());
            dtoList.add(DesignerResultDto.from(designer, isAffiliated));
        }
        return dtoList;


    }

    // 미용실 디자이너 등록 시 저장 메서드
    @Transactional
    public DesignerListDto addDesignerList(Long designerId, Long memberId){

        Designer designer = designerRepo.findById(designerId).orElseThrow();
        Shop shop = shopDesignerRepo.findByDesigner_Member_IdAndIsActiveTrue(memberId).getShop();

        if (shopDesignerRepo.existsByDesigner_Id(designerId)) {
            throw new IllegalArgumentException("이미 미용실에 소속된 디자이너입니다.");
        }

        ShopDesigner newDes = new ShopDesigner();
        newDes.setDesigner(designer);
        newDes.setShop(shop);
        newDes.setPosition("디자이너"); // default
        newDes.setScheduledStartTime(shop.getOpenTime()); // default = 오픈시간
        newDes.setScheduledEndTime(shop.getCloseTime().minusMinutes(shop.getTimeBeforeClosing())); // default = 예약마감시간
        newDes.setActive(true);

        shopDesignerRepo.save(newDes);

        DesignerService service = designerServiceRepo.findByShopDesignerId(newDes.getId()).orElse(null);
        int likeCount = salonLikeRepo.countByLikeTypeAndTypeId(LikeType.DESIGNER, newDes.getId());
        int reviewCount = reviewRepo.countByReservation_ShopDesigner_Id(newDes.getId());

        return DesignerListDto.from(newDes, likeCount, reviewCount, service);

    }

    // 미용실 디자이너 삭제 시
    @Transactional
    public void deleteDesigner(Long shopDesignerId){

        ShopDesigner shopDesigner = shopDesignerRepo.findByIdAndIsActiveTrue(shopDesignerId);
        shopDesigner.setActive(false);

    }

    // 시술 목록 가져오기
    public List<ServiceForm> getServiceList(Long memberId){

        Shop shop = shopDesignerRepo.findByDesigner_Member_IdAndIsActiveTrue(memberId).getShop();

        List<ShopService> serviceList = shopServiceRepo.findByShopId(shop.getId());
        List<ServiceForm> forms = new ArrayList<>();
        for(ShopService service : serviceList){
            forms.add(ServiceForm.from(service));
        }

        return forms;
    }

    // 시술 생성
    @Transactional
    public ServiceForm addService(Long memberId, ServiceForm form){

        Shop shop = shopDesignerRepo.findByDesigner_Member_IdAndIsActiveTrue(memberId).getShop();

        ShopService service = form.to(shop);
        if (form.getImgFile() != null && !form.getImgFile().isEmpty()) {
            UploadedFileDto fileDto = fileService.upload(form.getImgFile(), UploadType.SHOP_SERVICE);
            service.setOriginalImgName(fileDto.getOriginalFileName());
            service.setImgName(fileDto.getFileName());
            service.setImgUrl(fileDto.getFileUrl());
        }

        ShopService addedService = shopServiceRepo.save(service);

        return ServiceForm.from(addedService);
    }

    // 시술 수정 모달용
    public Optional<ServiceForm> getServiceEdit(Long serviceId){

        Optional<ShopService> shopServiceOptional = shopServiceRepo.findById(serviceId);

        if (shopServiceOptional.isPresent()) {
            ShopService shopService = shopServiceOptional.get();
            return Optional.of(ServiceForm.from(shopService));
        } else {
            return Optional.empty();
        }

    }

    // 시술 수정
    @Transactional
    public ServiceForm updateService(Long serviceId, ServiceForm form){

        ShopService service = shopServiceRepo.findById(serviceId).orElseThrow(()
                -> new IllegalArgumentException("존재하지 않는 시술"));

        ShopService updatedService = form.to(service.getShop());
        updatedService.setId(serviceId);

        if (form.getImgFile() != null && !form.getImgFile().isEmpty()) {
            UploadedFileDto fileDto = fileService.upload(form.getImgFile(), UploadType.SHOP_SERVICE);
            updatedService.setOriginalImgName(fileDto.getOriginalFileName());
            updatedService.setImgName(fileDto.getFileName());
            updatedService.setImgUrl(fileDto.getFileUrl());
        }

        shopServiceRepo.save(updatedService);

        return ServiceForm.from(updatedService);
    }

    // 시술 삭제
    @Transactional
    public void deleteService(Long serviceId){

        ShopService service = shopServiceRepo.findById(serviceId).orElseThrow(()
                -> new IllegalArgumentException("없는 서비스"));

        // 이미지 삭제
        if(service.getImgName() != null){
            Path filePath = Paths.get(UploadType.SHOP_SERVICE.getUrlPath(), service.getImgName());
            try {
                Files.deleteIfExists(filePath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        shopServiceRepo.delete(service);
    }

    // 디자이너별 출퇴근 기록
    public List<DesAttDto> getDesAttList(Long designerId) {

        ShopDesigner designer = shopDesignerRepo.findByDesigner_Member_IdAndIsActiveTrue(designerId);
        Shop shop = designer.getShop();

        // 미용실 소속 디자이너
        List<ShopDesigner> designers = shopDesignerRepo.findByShopIdAndIsActiveTrue(shop.getId());

        List<DesAttDto> dto = new ArrayList<>();

        // 각 디자이너 별 근태 목록
        for(ShopDesigner shopDesigner : designers){
            List<Attendance> attendances = attendanceRepo.findByShopDesignerIdOrderByIdDesc(shopDesigner.getId());
            List<AttendanceListDto> attendanceList = new ArrayList<>();
            for(Attendance attendance : attendances){
                attendanceList.add(AttendanceListDto.from(attendance));
            }
            dto.add(DesAttDto.from(shopDesigner, attendanceList));
        }

        return dto;
    }

    // 매출 내역 가져오기
    public SalesPageDto getSalesDashboard(Long shopDesignerId, int year, int month) {

        Shop shop = shopDesignerRepo.findByDesigner_Member_IdAndIsActiveTrue(shopDesignerId).getShop();

        YearMonth yearMonth = YearMonth.of(year, month);
        // 월간 기간 잡기
        LocalDateTime startOfMonth = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime endOfMonth = yearMonth.atEndOfMonth().atTime(23,59,59);

        // 매장 결제내역 가져오기
        List<Payment> payments = paymentRepo.findByShopDesigner_Shop_IdAndPayDateBetween(shop.getId(), startOfMonth, endOfMonth);

        // 총 매출액
        Long totalSales = 0L;

        // <카테고리, 총 금액>
        Map<ServiceCategory, Long> categorySalesMap = new EnumMap<>(ServiceCategory.class);
        // <ShopDesignerId, 총 금액>
        Map<Long, Long> designerSalesMap = new HashMap<>();
        // <ShopDesignerId, <카테고리, 총 금액> >
        Map<Long, Map<ServiceCategory, Long>> designerCategorySalesMap = new HashMap<>();

        // 카테고리별 맵 전부 0으로 초기화
        for (ServiceCategory category : ServiceCategory.values()) {
            categorySalesMap.put(category, 0L);
        }

        for(Payment payment : payments){
            Long totalPrice = (long) payment.getTotalPrice();
            ServiceCategory category = payment.getServiceCategory();
            Long designerId = payment.getShopDesigner().getId();

            // 총 매출
            totalSales += totalPrice;

            // 카테고리별 매출
            categorySalesMap.put(category, categorySalesMap.get(category) + totalPrice);

            // 디자이너 매출
            designerSalesMap.put(designerId, designerSalesMap.getOrDefault(designerId, 0L) + totalPrice);

            // 디자이너 카테고리별 매출
            Map<ServiceCategory, Long> categorySales = designerCategorySalesMap.get(designerId);

            // 처음 생성시 초기화 해주기
            if(categorySales == null){
                categorySales = new EnumMap<>(ServiceCategory.class);
                for (ServiceCategory serviceCategory : ServiceCategory.values()) {
                    categorySales.put(serviceCategory, 0L);
                }
                designerCategorySalesMap.put(designerId, categorySales);
            }
            categorySales.put(category, categorySales.get(category) + totalPrice);

        }

        // CategorySalesDto 만들기
        List<CategorySalesDto> categorySalesDtoList = new ArrayList<>();
        for(Map.Entry<ServiceCategory, Long> entry : categorySalesMap.entrySet()){
            categorySalesDtoList.add(CategorySalesDto.from(entry.getKey(), entry.getValue()));
        }
        
        // DesignerSalesDto List 만들기
        List<DesignerSalesDto> designerSalesDtoList = new ArrayList<>();
        
        // DesignerSalesMap 에 담긴 designerId 리스트
        List<Long> shopDesignerIds = new ArrayList<>(designerSalesMap.keySet());

        // <shopDesignerId, ShopDesigner> Map  에 담기
        Map<Long, ShopDesigner> shopDesignerMap = new HashMap<>();
        if(!shopDesignerIds.isEmpty()){
            List<ShopDesigner> shopDesigners = shopDesignerRepo.findAllById(shopDesignerIds);
            for(ShopDesigner designer : shopDesigners){
                shopDesignerMap.put(designer.getId(), designer);
            }
        }

        // 각 디자이너별 DesignerSalesDto 만들기
        for(Long designerId : shopDesignerMap.keySet()){
            ShopDesigner designer = shopDesignerMap.get(designerId);
            Long designerTotalSales = designerSalesMap.get(designerId);

            // 해당 디자이너의 List<CategorySales> 가져오기
            Map<ServiceCategory, Long> designerCategorySales = designerCategorySalesMap.get(designerId);
            List<CategorySalesDto> categorySales = new ArrayList<>();
            for(Map.Entry<ServiceCategory, Long> entry : designerCategorySales.entrySet()){
                categorySales.add(CategorySalesDto.from(entry.getKey(), entry.getValue()));
            }

            // 해당 디자이너의 DesignerSalesDto 를 리스트에 담기
            designerSalesDtoList.add(new DesignerSalesDto(designerId, designer.getDesigner().getMember().getName(),
                    designerTotalSales, categorySales));
        }

        return new SalesPageDto(totalSales, categorySalesDtoList, designerSalesDtoList);
    }

    // 매장 수정 시 ShopEditDto 보내기
    public ShopEditDto getShopEdit(Long memberId){

        ShopDesigner designer = shopDesignerRepo.findByDesigner_Member_IdAndIsActiveTrue(memberId);
        List<ShopImage> images = shopImageRepo.findByShopId(designer.getShop().getId());

        List<ShopImageDto> imageDtos = new ArrayList<>();
        for(ShopImage image : images){
            imageDtos.add(ShopImageDto.from(image));
        }

        return ShopEditDto.from(designer.getShop(), imageDtos);

    }

    // 매장 수정 저장
    @Transactional
    public void saveShopEdit(ShopEditDto dto, List<MultipartFile> files, List<Long> deletedImageIds, Long thumbnailImageId){

        Shop shop = shopRepo.findById(dto.getId()).orElse(null);
        shop = dto.to(shop);
        shopRepo.save(shop);

        // 기존 이미지 삭제 처리
        if (deletedImageIds != null) {
            for (Long id : deletedImageIds) {
                shopImageRepo.deleteById(id);
            }
        }

        // 새 이미지 저장 및 ID 매핑
        Map<String, Long> newImageIdMap = new HashMap<>();
        if (files != null && !files.isEmpty()) {
            for (MultipartFile file : files) {
                UploadedFileDto fileDto = fileService.upload(file, UploadType.SHOP);

                ShopImage image = new ShopImage();
                image.setShop(shop);
                image.setOriginalName(fileDto.getOriginalFileName());
                image.setImgName(fileDto.getFileName());
                image.setImgUrl(fileDto.getFileUrl());
                image.setIsThumbnail(false); // 기본 썸네일 아님

                shopImageRepo.save(image);

                // 고유 ID 매핑 → "new_filename_size"
                String key = "new_" + file.getOriginalFilename() + "_" + file.getSize();
                newImageIdMap.put(key, image.getId());
            }
        }

        // 썸네일 지정
        List<ShopImage> allImages = shopImageRepo.findByShopId(shop.getId());
        for (ShopImage img : allImages) {
            boolean isThumbnail = false;

            if (thumbnailImageId != null && img.getId().equals(thumbnailImageId)) {
                isThumbnail = true;
            } else if (dto.getThumbnailImageTempId() != null && dto.getThumbnailImageTempId().startsWith("new_")) {
                Long matchedId = newImageIdMap.get(dto.getThumbnailImageTempId());
                if (matchedId != null && img.getId().equals(matchedId)) {
                    isThumbnail = true;
                }
            }

            img.setIsThumbnail(isThumbnail);
            shopImageRepo.save(img);
        }

    }

    // 쿠폰 페이지 목록
    public List<CouponDto> getCouponList(Long memberId){

        ShopDesigner designer = shopDesignerRepo.findByDesigner_Member_IdAndIsActiveTrue(memberId);

        List<Coupon> coupons = couponRepo.findByShopIdAndIsActiveTrueOrderByExpireDate(designer.getId());

        List<CouponDto> dtoList = new ArrayList<>();
        for(Coupon coupon : coupons){
            dtoList.add(CouponDto.from(coupon));
        }

        return dtoList;
    }

    // 쿠폰 등록 메서드
    @Transactional
    public void saveCoupon(CouponDto dto, Long memberId){

        ShopDesigner designer = shopDesignerRepo.findByDesigner_Member_IdAndIsActiveTrue(memberId);

        couponRepo.save(dto.to(designer.getShop()));

    }

    // 쿠폰 만료일 될 시 isActive == false
    @Transactional
    @Scheduled(cron = "0 0 0 * * ?") // 매일 자정 실행
    public void expiredCoupons() {
        List<Coupon> expiredCoupons = couponRepo.findByExpireDateBeforeAndIsActiveTrue(LocalDate.now());
        for (Coupon coupon : expiredCoupons) {
            coupon.setActive(false);
            couponRepo.save(coupon);
        }
    }



}
