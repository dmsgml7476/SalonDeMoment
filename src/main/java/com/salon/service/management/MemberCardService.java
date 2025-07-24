package com.salon.service.management;

import com.salon.repository.management.MemberCardRepo;
import com.salon.repository.management.MemberMemoRepo;
import com.salon.repository.management.ShopDesignerRepo;
import com.salon.repository.shop.ReservationRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberCardService {

    private final ShopDesignerRepo shopDesignerRepo;
    private final ReservationRepo reservationRepo;
    private final MemberCardRepo memberCardRepo;
    private final MemberMemoRepo memberMemoRepo;






}
