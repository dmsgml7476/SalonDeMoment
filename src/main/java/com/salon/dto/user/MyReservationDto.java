package com.salon.dto.user;

import com.salon.constant.CouponType;
import com.salon.constant.ReservationStatus;

import com.salon.entity.Review;
import com.salon.entity.management.Payment;
import com.salon.entity.shop.Reservation;
import com.salon.repository.management.PaymentRepo;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;


@Getter
@Setter
public class MyReservationDto {

    private Long reservationId;  // 예약 id

    private String designerName;  // 해당 예약이 잡힌 디자이너 이름
    private String position;

    private Long shopId;

    private String shopName; // 샵 이름
    private String serviceName; // 시술 이름
    private LocalDateTime reservationDate; // 예약 날짜
    private ReservationStatus status; // 예약상태

    private int servicePrice;  //시술 가격

//    쿠폰 관련

    private boolean couponUsed;  //쿠폰 사용여부
    private CouponType couponType; // 쿠폰 타입
    private String couponName; // 사용쿠폰 이름
    private Integer daysExpire; // 남은 날짜
    private int couponDiscount; // 적용, 쿠폰 할인 금액
    private int discountValue; // 쿠폰 할인 양
    private int couponMin;  // 최소 사용 금액

//    정액권 관련

    private boolean ticketUsed; //정액권 사용 여부
    private int ticketUsedAmount; // 정액권 사용금
    private int ticketTotalAmount; // 티켓 원래 금액
    private int ticketNowAmount; // 티켓 현재 잔액
    private int ticketFinalAmount; // 티켓 예정 잔액

    //최종 결제액

    private int finalPrice; // 결제 예정액/최종 결제액

    //샵 정보 자세히

    private String shopAddress;  // 샵 주소
    private String shopTel; // 샵 전화번호

    //리뷰 관련

    private boolean reviewExists;  // 리뷰 존재여부
    private boolean canWriteReview;  // 리뷰 작성 가능여부
    private Long reviewId; // 리뷰 아이디

    //결제 정보
    private MyPaymentDto myPaymentDto;


    public static MyReservationDto from(Reservation reservation, Review review, PaymentRepo paymentRepo) {
        MyReservationDto dto = new MyReservationDto();

        dto.setShopId(reservation.getShopDesigner().getShop().getId());

        dto.setReservationId(reservation.getId());

        dto.setDesignerName(reservation.getShopDesigner().getDesigner().getMember().getName());
        dto.setPosition(reservation.getShopDesigner().getPosition());

        dto.setShopName(reservation.getShopDesigner().getShop().getName());

        dto.setServiceName(reservation.getShopService() != null ? reservation.getShopService().getName() : reservation.getServiceName());
        dto.setReservationDate(reservation.getReservationDate());
        dto.setStatus(reservation.getStatus());

        dto.setServicePrice(reservation.getShopService().getPrice());

        if(reservation.getCoupon() != null && reservation.getCoupon().getExpireDate() != null) {
            dto.setCouponUsed(true);
            dto.setCouponType(reservation.getCoupon().getDiscountType());
            dto.setCouponName(reservation.getCoupon().getName());
            dto.setCouponDiscount(reservation.getDiscountAmount());
            dto.setDiscountValue(reservation.getCoupon().getDiscountValue());
            dto.setCouponMin(reservation.getCoupon().getMinimumAmount());

            LocalDate today = LocalDate.now();
            LocalDate expireDate = reservation.getCoupon().getExpireDate();


            // 쿠폰
            if(expireDate.isBefore(today)) {
                dto.setDaysExpire(0);
            } else {
                dto.setDaysExpire((int) ChronoUnit.DAYS.between(today, expireDate));
            }

        } else {
            dto.setCouponUsed(false);
            dto.setCouponName(null);
            dto.setCouponDiscount(0);
            dto.setCouponType(null);
            dto.setDaysExpire(null);
            dto.setDiscountValue(0);
        }

        //정액권
        if(reservation.getTicket() != null) {
            dto.setTicketUsed(true);
            dto.setTicketUsedAmount(reservation.getTicketUsedAmount());
            dto.setTicketTotalAmount(reservation.getTicket().getTotalAmount());
            dto.setTicketNowAmount(dto.getTicketTotalAmount() - reservation.getTicket().getUsedAmount());
            dto.setTicketFinalAmount(dto.getTicketNowAmount() - dto.getTicketUsedAmount());
        }

        int discountAmount = reservation.getDiscountAmount();
        int ticketUsed = reservation.getTicketUsedAmount();
        dto.setFinalPrice(dto.getServicePrice() - discountAmount - ticketUsed);

        dto.setShopAddress(reservation.getShopDesigner().getShop().getAddress());
        dto.setShopTel(reservation.getShopDesigner().getShop().getTel());


        //리뷰
        if(review != null) {
            dto.setReviewExists(true);
            dto.setReviewId(review.getId());
        } else {
            dto.setReviewExists(false);
            dto.setReviewId(null);
        }

        boolean isCompleted = reservation.getStatus() == ReservationStatus.COMPLETED;
        boolean within5Days = reservation.getReservationDate().plusDays(5).isAfter(LocalDateTime.now());
        dto.setCanWriteReview(isCompleted && within5Days && !dto.isReviewExists());


        // 결제 정보

        paymentRepo.findByReservationId(reservation.getId()).ifPresent(payment -> {
            MyPaymentDto paymentDto = new MyPaymentDto();
            paymentDto.setReservationId(reservation.getId());
            paymentDto.setPayServiceName(payment.getReservation() != null  ?
                    payment.getReservation().getShopService().getName() : payment.getServiceName()); // 실제 결제된 시술명
            paymentDto.setCouponDiscountPrice(payment.getCouponDiscountPrice());
            paymentDto.setTicketUsedPrice(payment.getTicketUsedPrice());
            paymentDto.setFinalPrice(payment.getFinalPrice());
            paymentDto.setTotalPrice(payment.getTotalPrice());

            paymentDto.setPaymentType(payment.getPaymentType());

            dto.setMyPaymentDto(paymentDto);
        });

        return dto;

    }



}
