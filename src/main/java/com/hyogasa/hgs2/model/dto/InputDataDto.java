package com.hyogasa.hgs2.model.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class InputDataDto {
//    @Builder
//    public InputData(String cre_date, String name, double price, double volume) {
//        this.cre_date = cre_date;
//        this.name = name;
//        this.price = price;
//        this.volume = volume;
//    }

    private String cre_date;
    private String name;
    private String price;
    private String volume;
    private String regdate;
    private String x5chk;
    private String x10chk;
    private String xxchk;

    @Builder
    public InputDataDto(String cre_date, String name, String price, String volume, String regdate) {
        this.cre_date = cre_date;
        this.name = name;
        this.price = price;
        this.volume = volume;
        this.regdate = regdate;
    }



}
