package com.salon.service.admin;

import com.salon.constant.*;
import com.salon.dto.BizCheckRequestDto;
import com.salon.dto.BizStatusDto;
import com.salon.dto.UploadedFileDto;
import com.salon.dto.admin.*;
import com.salon.entity.Member;
import com.salon.entity.admin.Apply;
import com.salon.entity.admin.CouponBanner;
import com.salon.entity.admin.CsCustomer;
import com.salon.entity.admin.CsFile;
import com.salon.entity.management.Designer;
import com.salon.entity.management.ShopDesigner;
import com.salon.entity.management.master.Coupon;
import com.salon.entity.shop.Shop;
import com.salon.repository.MemberRepo;
import com.salon.repository.admin.ApplyRepo;
import com.salon.repository.admin.CouponBannerRepo;
import com.salon.repository.admin.CsCustomerFileRepo;
import com.salon.repository.admin.CsCustomerRepo;
import com.salon.repository.management.DesignerRepo;
import com.salon.repository.management.ShopDesignerRepo;
import com.salon.repository.management.master.CouponRepo;
import com.salon.repository.shop.ShopRepo;
import com.salon.util.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CsService {
    private final CsCustomerRepo csCustomerRepo;
    private final CsCustomerFileRepo csCustomerFileRepo;
    private final ApplyRepo applyRepo;
    private final MemberRepo memberRepo;
    private final CouponRepo couponRepo;
    private final FileService fileService;
    private final CouponBannerRepo couponBannerRepo;
    private final ShopRepo shopRepo;
    private final ShopDesignerRepo shopDesignerRepo;
    private final DesignerRepo designerRepo;


    public void questionSave(CsCreateDto csCreateDto, Member member, List<MultipartFile> files) {
        CsCustomer csCustomer = CsCreateDto.to(csCreateDto, member);
        csCustomer = csCustomerRepo.save(csCustomer);
        if(files != null && !files.isEmpty()){
            for(MultipartFile file : files){
                if(!file.isEmpty()){
                    UploadedFileDto image = fileService.upload(file, UploadType.CUSTOMER_SERVICE);

                    CsFile csFile = new CsFile();
                    csFile.setOriginalName(image.getOriginalFileName());
                    csFile.setFileName(image.getFileName());
                    csFile.setFileUrl(image.getFileUrl());
                    csFile.setCsCustomer(csCustomer);

                    csCustomerFileRepo.save(csFile);
                }
            }
        }
    }

    public List<CsListDto> List() {
        List<CsCustomer> csCustomerList = csCustomerRepo.findAll();
        List<CsListDto> csListDtoList = new ArrayList<>();
        for(CsCustomer csCustomer:csCustomerList){
            CsListDto csListDto = CsListDto.from(csCustomer);
            csListDtoList.add(csListDto);
        }
        return csListDtoList;
    }

    public CsCreateDto create(Long id) {
        CsCustomer csCustomer = csCustomerRepo.findById(id).get();
        CsCreateDto csCreateDto = CsCreateDto.from(csCustomer);
        return csCreateDto;
    }

    public CsListDto list(Long id) {
        CsCustomer csCustomer = csCustomerRepo.findById(id).get();
        CsListDto csListDto = CsListDto.from(csCustomer);
        return csListDto;
    }

    public Long replySave(CsDetailDto csDetailDto, CsCreateDto csCreateDto, CsListDto csListDto) {
        Member admin = memberRepo.findByLoginId(csDetailDto.getLoginId());

        CsCustomer customer = csCustomerRepo.findById(csDetailDto.getId())
                        .orElseThrow(() -> new RuntimeException("문의 내역을 찾을 수 없습니다."));
        customer.setReplyAt(LocalDateTime.now());
        customer.setReplyText(csDetailDto.getReplyText());
        customer.setStatus(CsStatus.COMPLETED);
        customer.setAdmin(admin);
        csCustomerRepo.save(customer);
        return customer.getId();
    }

    public CsDetailDto detail(Long id) {
        CsCustomer csCustomer = csCustomerRepo.findById(id).get();
        CsDetailDto csDetailDto = CsDetailDto.from(csCustomer);
        return csDetailDto;
    }
    @Transactional
    public void registration(ApplyDto applyDto, Member member) {
        boolean alreadyApplied = applyRepo.existsByMemberAndApplyType(member, ApplyType.SHOP);
        if(alreadyApplied){
            throw new IllegalStateException("이미 신청한 내역이 있습니다.");
        }

        BizStatusDto.BizInfo bizInfo = bizApiCheck(applyDto.getApplyNumber());

        if(bizInfo != null && "계속사업자".equals(bizInfo.getB_stt())) {
            System.out.println("정상 사업자가 아니므로 저장은 하지만, 상태는 확인 요망: " + applyDto.getApplyNumber());
        }
        Apply apply = new Apply();
        apply.setApplyNumber(applyDto.getApplyNumber());
        apply.setStatus(ApplyStatus.WAITING);
        apply.setApplyType(ApplyType.SHOP);
        apply.setCreateAt(LocalDateTime.now());
        apply.setIssuedDate(LocalDate.now().toString());
        apply.setMember(member);
        System.out.println("저장 시도: applyNumber=" + apply.getApplyNumber()
                + ", status=" + apply.getStatus()
                + ", applyType=" + apply.getApplyType()
                + ", createAt=" + apply.getCreateAt()
                + ", issuedDate=" + apply.getIssuedDate()
                + ", memberId=" + (apply.getMember() != null ? apply.getMember().getId() : "null"));
        applyRepo.save(apply);

    }

    private BizStatusDto.BizInfo bizApiCheck(String bizNo) {
        try{
            WebClient webClient = WebClient.create();
            BizCheckRequestDto request = new BizCheckRequestDto(Arrays.asList(bizNo));

            BizStatusDto response = webClient.post()
                    .uri("https://api.odcloud.kr/api/nts-businessman/v1/status?serviceKey=YOUR_KEY")
                    .header("Content-Type", "application/json")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(BizStatusDto.class)
                    .block();

            if(response != null && response.getData() != null && !response.getData().isEmpty()){
                return response.getData().get(0);
            }
        } catch (Exception e){
            System.out.println("사업자 상태 조회 실패" + e.getMessage());
        }
        return null;
    }

    public List<Apply> listShop() {
        return applyRepo.findByApplyTypeAndStatus(ApplyType.SHOP, ApplyStatus.WAITING);
    }

    public Long approveShop(Long id, Member admin) {
        Apply apply = applyRepo.findById(id)
                .orElseThrow(()->new IllegalArgumentException("신청 정보를 찾을 수 없습니다."));
        apply.setStatus(ApplyStatus.APPROVED);
        apply.setApproveAt(LocalDateTime.now());
        apply.setAdmin(admin);
        Member member = apply.getMember();
        member.setRole(Role.MAIN_DESIGNER);
        apply.setApplyType(ApplyType.SHOP);

        Shop shop = new Shop();
        shop.setName("");
        shop.setTel("");
        shop.setDayOff(127);
        shop.setTimeBeforeClosing(0);
        shop.setOpenTime(LocalTime.of(9,0));
        shop.setCloseTime(LocalTime.of(18,0));
        shopRepo.save(shop);

        ShopDesigner shopDesigner = new ShopDesigner();
        shopDesigner.setShop(shop);
        Designer designer = designerRepo.findByMember_Id(member.getId());
        shopDesigner.setDesigner(designer);
        shopDesigner.setActive(true);
        shopDesigner.setPosition("원장");
        shopDesigner.setScheduledStartTime(shop.getOpenTime());
        shopDesigner.setScheduledEndTime(shop.getCloseTime());
        shopDesignerRepo.save(shopDesigner);




        memberRepo.save(member);
        applyRepo.save(apply);

        return member.getId();
    }

    public Long rejectShop(Long id, Member admin) {
        Apply apply = applyRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("신청 정보를 찾을 수 없습니다."));
        apply.setStatus(ApplyStatus.REJECTED);
        apply.setApproveAt(LocalDateTime.now());
        apply.setAdmin(admin);
        applyRepo.save(apply);

        return apply.getMember().getId();
    }

    public void applyBanner(BannerApplyDto bannerApplyDto, Long memberId, MultipartFile file) {
        Coupon coupon = couponRepo.findById(bannerApplyDto.getCouponId())
                .orElseThrow(()-> new IllegalArgumentException("선택된 쿠폰이 유효하지 않습니다: " + bannerApplyDto.getCouponId()));
        Member member = memberRepo.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("신청자 회원 정보를 찾을 수 없습니다."));

       /* UploadedFileDto image = fileService.upload(file, UploadType.CUSTOMER_SERVICE);*/



        CouponBanner couponBanner = new CouponBanner();
        couponBanner.setCoupon(coupon);
        couponBanner.setStartDate(bannerApplyDto.getStartDate());
        couponBanner.setEndDate(bannerApplyDto.getEndDate());
        String originalFileName = file.getOriginalFilename();
        couponBanner.setOriginalName(originalFileName);
        String uuidFileName = UUID.randomUUID() + ".png";
        String fullPath = "C:/salon/csFile/" + uuidFileName;
        try {
            file.transferTo(new File(fullPath));
        } catch(IOException | IllegalStateException e){
            throw new RuntimeException("파일 저장 실패" + e.getMessage(), e);
        }
        String imgUrl = "/csFile/" + uuidFileName;
        couponBanner.setImgName(uuidFileName);
        couponBanner.setImgUrl(imgUrl);
        couponBanner.setCreatedAt(LocalDateTime.now());
        couponBanner.setStatus(ApplyStatus.WAITING);

        couponBannerRepo.save(couponBanner);
    }

    public CouponBannerDetailDto bannerDetail(Long id) {
        CouponBanner couponBanner = couponBannerRepo.findById(id).orElseThrow();
        CouponBannerDetailDto couponBannerDetailDto = CouponBannerDetailDto.from(couponBanner);
        return couponBannerDetailDto;
    }

    public List<CsListDto> findAll() {
        return csCustomerRepo.findAll().stream()
                .map(CsListDto::from)
                .collect(Collectors.toList());
    }

    public List<CsListDto> findByMember(Member member) {
        List<CsCustomer> csCustomerList = csCustomerRepo.findByMember(member);
        return csCustomerList.stream()
                .map(CsListDto::from)
                .collect(Collectors.toList());
    }

    public List<CouponBannerListDto> bannerList() {
        List<CouponBanner> couponBannerList = couponBannerRepo.findAll();
        List<CouponBannerListDto> couponBannerListDtoList = new ArrayList<>();
        for(CouponBanner couponBanner : couponBannerList){
            CouponBannerListDto couponBannerListDto = CouponBannerListDto.from(couponBanner);
            couponBannerListDtoList.add(couponBannerListDto);
        }
        return couponBannerListDtoList;
    }

    public Long bannerApprove(Long id, Member admin) {
        CouponBanner couponBanner = couponBannerRepo.findById(id).orElseThrow();
        couponBanner.setRegisterDate(LocalDateTime.now());
        couponBanner.setStatus(ApplyStatus.APPROVED);
        couponBanner.setAdmin(admin);
        couponBannerRepo.save(couponBanner);

        // 웹 알림 관련 코드
        Long shopId = couponBanner.getCoupon().getShop().getId();

        ShopDesigner shopDesigner = shopDesignerRepo.findFirstByShopIdAndIsActiveTrueAndDesigner_Member_Role(shopId, Role.MAIN_DESIGNER);

        if (shopDesigner == null) {
            throw new RuntimeException("MAINDESIGNER 역할의 디자이너가 없습니다.");
        }


        Long receiverId = shopDesigner.getDesigner().getMember().getId();


        return receiverId;
    }

    public Long bannerReject(Long id, Member admin) {
        CouponBanner couponBanner = couponBannerRepo.findById(id).orElseThrow();
        couponBanner.setRegisterDate(LocalDateTime.now());
        couponBanner.setStatus(ApplyStatus.REJECTED);
        couponBanner.setAdmin(admin);


        // 웹 알림 관련 코드
        Long shopId = couponBanner.getCoupon().getShop().getId();

        ShopDesigner shopDesigner = shopDesignerRepo.findFirstByShopIdAndIsActiveTrueAndDesigner_Member_Role(shopId, Role.MAIN_DESIGNER);

        if (shopDesigner == null) {
            throw new RuntimeException("MAINDESIGNER 역할의 디자이너가 없습니다.");
        }


        Long receiverId = shopDesigner.getDesigner().getMember().getId();


        couponBannerRepo.save(couponBanner);

        return receiverId;
    }
}
