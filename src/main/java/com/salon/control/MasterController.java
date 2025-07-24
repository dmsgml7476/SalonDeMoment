package com.salon.control;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.salon.config.CustomUserDetails;
import com.salon.dto.designer.DesignerListDto;
import com.salon.dto.management.ServiceForm;
import com.salon.dto.management.master.*;
import com.salon.entity.management.ShopDesigner;
import com.salon.entity.management.master.Coupon;
import com.salon.entity.shop.ShopImage;
import com.salon.service.management.master.MasterService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
@AllArgsConstructor
@RequestMapping("/master")
public class MasterController {

    private final MasterService masterService;

    // 메인페이지
    @GetMapping("")
    public String getMain(@AuthenticationPrincipal CustomUserDetails userDetails, Model model){


        model.addAttribute("dto", masterService.getMainPage(userDetails.getMember().getId()));

        return "master/main";
    }

    // 디자이너 관리
    @GetMapping("/designer-list")
    public String designerList(@AuthenticationPrincipal CustomUserDetails userDetails,
                               Model model){

        List<DesignerListDto> designerList = masterService.getDesignerList(userDetails.getMember().getId());

        model.addAttribute("designerList", designerList);
        model.addAttribute("designerSearch", new DesignerSearchDto());

        return "master/designerList";
    }

    // 미용실 디자이너 검색 후 목록 보여주기
    // 디자이너 검색 API 엔드포인트
    @GetMapping("/designer-search")
    public ResponseEntity<List<DesignerResultDto>> searchDesigners(@ModelAttribute DesignerSearchDto searchDto) {

        // 서비스 계층의 검색 메서드 호출
        List<DesignerResultDto> foundDesigners = masterService.getDesignerResult(searchDto);

        return ResponseEntity.ok(foundDesigners);
    }

    // 디자이너 추가 API
    @PostMapping("/add-designer")
    public ResponseEntity<DesignerListDto> addDesignerToShop(
            @RequestParam Long designerId, // <-- DesignerAddRequestDto 대신 @RequestParam으로 직접 받습니다.
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        try {
            // 서비스 메서드 호출 시 designerId를 직접 전달합니다.
            DesignerListDto newShopDesignerDto = masterService.addDesignerList(designerId, userDetails.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(newShopDesignerDto);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            System.err.println("디자이너 추가 중 서버 오류: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // 디자이너 수정 페이지
    @GetMapping("/designer/edit")
    public String designerEdit(){

        return "master/designerEdit";
    }


    // 시술 관리 페이지
    @GetMapping("/services")
    public String reservations(@AuthenticationPrincipal CustomUserDetails userDetails,
                               Model model){

        List<ServiceForm> serviceList = masterService.getServiceList(userDetails.getId());

        model.addAttribute("serviceList", serviceList);

        return "master/services";
    }

    // 시술 저장
    @PostMapping("/services")
    @ResponseBody
    public ResponseEntity<ServiceForm> createService(@ModelAttribute ServiceForm serviceForm,
                                                     @AuthenticationPrincipal CustomUserDetails userDetails) {

        ServiceForm createdService = masterService.addService(userDetails.getId(), serviceForm);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdService);
    }

    // 시술 수정 모달
    @GetMapping("/services/{serviceId}")
    @ResponseBody
    public ResponseEntity<ServiceForm> getServiceDetails(@PathVariable Long serviceId) {

        Optional<ServiceForm> serviceFormOptional = masterService.getServiceEdit(serviceId);

        if (serviceFormOptional.isPresent()) {
            return ResponseEntity.ok(serviceFormOptional.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // 시술 수정
    @PutMapping("/services/{serviceId}")
    @ResponseBody
    public ResponseEntity<ServiceForm> updateService(
            @PathVariable Long serviceId,
            @ModelAttribute ServiceForm serviceForm) {
        ServiceForm updatedService = masterService.updateService(serviceId, serviceForm);
        return ResponseEntity.ok(updatedService);

    }

    // 시술 삭제
    @DeleteMapping("/services/{serviceId}")
    @ResponseBody
    public ResponseEntity<Void> deleteService(@PathVariable Long serviceId) {
        masterService.deleteService(serviceId);
        return ResponseEntity.noContent().build();
    }

    // 출퇴근 내역 페이지
    @GetMapping("/attendances")
    public String attendance(){

        return "master/attendances";
    }

    // 출퇴근 내역 디자이너 목록 api
    @GetMapping("/attDes")
    public ResponseEntity<List<DesAttDto>> getAttDes(@AuthenticationPrincipal CustomUserDetails userDetails){

        List<DesAttDto> desAttList = masterService.getDesAttList(userDetails.getId());

        return new ResponseEntity<>(desAttList, HttpStatus.OK);
    }

    // 매장 매출 내역
    @GetMapping("/sales")
    public String getSales(){

        return "/master/sales";
    }

    // 매장 매출 api
    @GetMapping("/sales-dashboard")
    public ResponseEntity<SalesPageDto> salesDashboard(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                       @RequestParam int year,
                                                       @RequestParam int month){

        SalesPageDto salesPageDto = masterService.getSalesDashboard(userDetails.getId(), year, month);

        return ResponseEntity.ok(salesPageDto);

    }


    // 매장 관리
    @GetMapping("/shop-edit")
    public String shopEdit(Model model, @AuthenticationPrincipal CustomUserDetails userDetails){

        ShopEditDto shopEdit = masterService.getShopEdit(userDetails.getMember().getId());

        model.addAttribute("shop", shopEdit);

        return "master/shopEdit";
    }

    // 매장 수정 저장
    @PostMapping("/shop-edit/update")
    public String saveShop(
            @RequestParam("shopEditDto") String shopEditDtoJson,
            @RequestParam(value = "files", required = false) List<MultipartFile> files,
            @RequestParam(value = "deletedImageIds", required = false) String deletedImageIdsJson,
            @RequestParam(value = "thumbnailImageId", required = false) String thumbnail
    ) throws JsonProcessingException {

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        // DTO 파싱
        ShopEditDto dto = objectMapper.readValue(shopEditDtoJson, ShopEditDto.class);

        // 삭제 이미지 ID 파싱
        List<Long> deletedImageIds = new ArrayList<>();
        if (deletedImageIdsJson != null && !deletedImageIdsJson.isEmpty()) {
            deletedImageIds = objectMapper.readValue(deletedImageIdsJson, new TypeReference<List<Long>>() {});
        }

        // 썸네일 ID 처리
        Long thumbnailImageId = null;
        String thumbnailTempId = null;

        if (thumbnail != null && !thumbnail.isEmpty()) {
            if (thumbnail.startsWith("new_")) {
                thumbnailTempId = thumbnail; // new_sample.jpg_1345123
            } else if (thumbnail.matches("\\d+")) {
                thumbnailImageId = Long.parseLong(thumbnail); // 기존 이미지 ID
            }
        }
        dto.setThumbnailImageTempId(thumbnailTempId);


        masterService.saveShopEdit(dto, files, deletedImageIds, thumbnailImageId);

        return "redirect:/master";
    }

    // 쿠폰 관리
    @GetMapping("/coupons")
    public String coupon(Model model, @AuthenticationPrincipal CustomUserDetails userDetails){

        model.addAttribute("couponList", masterService.getCouponList(userDetails.getMember().getId()));
        model.addAttribute("couponDto", new CouponDto());

        return "master/coupons";
    }

    // 쿠폰 등록
    @PostMapping("coupons/new")
    public String newCoupon(@Valid CouponDto dto, @AuthenticationPrincipal CustomUserDetails userDetails){

        masterService.saveCoupon(dto, userDetails.getMember().getId());

        return "redirect:/master/coupons";
    }

}
