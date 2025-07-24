package com.salon.service.user;


import com.salon.dto.user.MyReservationDto;
import com.salon.entity.Review;
import com.salon.entity.shop.Reservation;
import com.salon.repository.ReviewRepo;
import com.salon.repository.management.PaymentRepo;
import com.salon.repository.shop.ReservationRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MyReservationService {
    private final ReservationRepo reservationRepo;
    private final ReviewRepo reviewRepo;
    private final PaymentRepo paymentRepo;

    public List<MyReservationDto> getMyReservations (Long memberId) {
        List<Reservation> reservations = reservationRepo.findByMemberIdOrderByReservationDateDesc(memberId);

        return reservations.stream()
                .map(reservation -> {
                    Review review = reviewRepo.findByReservationId(reservation.getId()).orElse(null);
                    return MyReservationDto.from(reservation, review, paymentRepo);
                })
                .toList();
    }
}
