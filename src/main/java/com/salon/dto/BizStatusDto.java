package com.salon.dto;

import lombok.Data;

import java.util.List;

@Data
public class BizStatusDto {
    private String status_code;
    private int match_cnt;
    private int request_cnt;
    private List<BizInfo> data;

    @Data
    public static class BizInfo {
        private String b_no;       // 사업자번호
        private String b_stt;      // 상태 (계속사업자, 폐업자 등)
        private String b_stt_cd;
        private String tax_type;   // 과세유형 (일반과세자 등)
        private String tax_type_cd;
        private String end_dt;
        private String utcc_yn;
        private String tax_type_change_dt;
        private String invoice_apply_dt;
    }
}
