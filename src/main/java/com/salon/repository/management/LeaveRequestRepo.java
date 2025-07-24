package com.salon.repository.management;

import com.salon.entity.management.LeaveRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LeaveRequestRepo extends JpaRepository<LeaveRequest, Long> {

    // 미용실 휴가 목록
    List<LeaveRequest> findByShopDesigner_Shop_IdOrderByRequestAtDesc(Long shopId);

}
