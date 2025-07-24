package com.salon.dto.management.master;

import com.salon.entity.shop.Shop;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalTime;
import java.util.List;

@Getter @Setter
@ToString
public class ShopEditDto {

    private Long id; // 매장 ID

    @NotEmpty
    private String name;
    private String address;
    private String addressDetail;

    private String tel;

    private LocalTime openTime;
    private LocalTime closeTime;

    private String description;

    private Integer timeBeforeClosing; // 마감전 예약불가 시간 (분)
    private Integer reservationInterval; // 예약 간격 (분)

    private Integer dayOff;             // 정기 휴무일 (이진법 요일 저장)
    private Integer lateMin;           // 지각 기준 (분)
    private Integer earlyLeaveMin;     // 조퇴 기준 (분)

    private BigDecimal latitude;       // 위도 (카카오맵용)
    private BigDecimal longitude;      // 경도 (카카오맵용)

    // 기존 이미지
    private List<ShopImageDto> shopImages;

    // "new_xxx"용 임시 ID
    private String thumbnailImageTempId;


    public static ShopEditDto from(Shop shop, List<ShopImageDto> images) {
        ShopEditDto dto = new ShopEditDto();

        // detail 뺀 주소 출력하기

        String fullAddress = shop.getAddress();       // 도로명 + 상세주소 합쳐진 문자열
        String detail = shop.getAddressDetail();      // 상세주소

        dto.setAddressDetail(detail);

        if (fullAddress != null && detail != null && fullAddress.endsWith(detail)) {
            // 상세주소가 주소 끝에 붙어있으면 잘라내기
            dto.setAddress(fullAddress.substring(0, fullAddress.length() - detail.length()).trim());
        } else {
            // 아니면 그냥 전체 주소 넣기
            dto.setAddress(fullAddress);
        }

        dto.setId(shop.getId());
        dto.setName(shop.getName());
        dto.setAddressDetail(shop.getAddressDetail());
        dto.setTel(shop.getTel());
        dto.setOpenTime(shop.getOpenTime());
        dto.setCloseTime(shop.getCloseTime());
        dto.setDescription(shop.getDescription());
        dto.setTimeBeforeClosing(shop.getTimeBeforeClosing());
        dto.setReservationInterval(shop.getReservationInterval());
        dto.setDayOff(shop.getDayOff());
        dto.setLateMin(shop.getLateMin());
        dto.setEarlyLeaveMin(shop.getEarlyLeaveMin());
        dto.setLatitude(shop.getLatitude());
        dto.setLongitude(shop.getLongitude());
        dto.setShopImages(images);

        return dto;
    }

    // DTO → 기존 엔티티 수정 (수정용)
    public Shop to(Shop shop) {
        shop.setName(this.name);
        shop.setAddress(this.address);
        shop.setAddressDetail(this.addressDetail);
        shop.setTel(this.tel);
        shop.setOpenTime(this.openTime);
        shop.setCloseTime(this.closeTime);
        shop.setDescription(this.description);
        shop.setTimeBeforeClosing(this.timeBeforeClosing);
        shop.setReservationInterval(this.reservationInterval);
        shop.setDayOff(this.dayOff);
        shop.setLateMin(this.lateMin);
        shop.setEarlyLeaveMin(this.earlyLeaveMin);
        shop.setLatitude(this.latitude.setScale(7, RoundingMode.HALF_UP));
        shop.setLongitude(this.longitude.setScale(7, RoundingMode.HALF_UP));
        return shop;
    }




}
