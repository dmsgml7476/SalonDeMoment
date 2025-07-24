package com.salon.dto.management;

import com.salon.constant.LeaveStatus;
import com.salon.constant.LeaveType;
import com.salon.entity.management.LeaveRequest;
import com.salon.entity.management.ShopDesigner;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter @Setter
public class LeaveRequestDto {

    private Long id; // LeaveRequest Id
    private Long shopDesignerId; // ShopDesignerId
    private String designerName;
    private LocalDate startDate;
    private LocalDate endDate;
    private LeaveType leaveType;
    private String reason;
    private LeaveStatus status;
    private LocalDateTime requestAt;
    private LocalDateTime approvedAt;

    public static LeaveRequestDto from(LeaveRequest leaveRequest) {

        LeaveRequestDto dto = new LeaveRequestDto();

        dto.setId(leaveRequest.getId());
        dto.setShopDesignerId(leaveRequest.getShopDesigner().getId());
        dto.setDesignerName(leaveRequest.getShopDesigner().getDesigner().getMember().getName());
        dto.setStartDate(leaveRequest.getStartDate());
        dto.setEndDate(leaveRequest.getEndDate());
        dto.setLeaveType(leaveRequest.getLeaveType());
        dto.setReason(leaveRequest.getReason());
        dto.setStatus(leaveRequest.getStatus());
        dto.setRequestAt(leaveRequest.getRequestAt());
        dto.setApprovedAt(leaveRequest.getApprovedAt());

        return dto;

    }

    public LeaveRequest to (ShopDesigner designer) {

        LeaveRequest request = new LeaveRequest();

        request.setShopDesigner(designer);
        request.setStartDate(this.startDate);
        request.setEndDate(this.endDate);
        request.setLeaveType(this.leaveType);
        request.setRequestAt(LocalDateTime.now());

        return request;
    }




}
