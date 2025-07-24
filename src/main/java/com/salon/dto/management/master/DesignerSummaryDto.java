package com.salon.dto.management.master;

import com.salon.entity.management.ShopDesigner;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class DesignerSummaryDto {

    private Long id;

    private String name;
    private String imgUrl;
    private String position;
    private int workingYears;
    private int todayReservationCount;   // 당일 예약 수

    private String scheduledWorkTime;    // ex) "09:30~18:30"

    public static DesignerSummaryDto from(ShopDesigner designer, int todayReservationCount){

        DesignerSummaryDto dto = new DesignerSummaryDto();

        dto.setId(designer.getDesigner().getId());
        dto.setName(designer.getDesigner().getMember().getName());
        dto.setImgUrl(designer.getDesigner().getImgUrl());
        dto.setPosition(designer.getPosition());
        dto.setWorkingYears(designer.getDesigner().getWorkingYears());
        dto.setTodayReservationCount(todayReservationCount);

        // 출퇴근 시간 문자열 포맷팅
        if (designer.getScheduledStartTime() != null && designer.getScheduledEndTime() != null) {
            String start = designer.getScheduledStartTime().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"));
            String end = designer.getScheduledEndTime().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"));
            dto.setScheduledWorkTime(start + " ~ " + end);
        } else {
            dto.setScheduledWorkTime(""); // 비어있거나 기본값 처리
        }

        return dto;


    }


}
