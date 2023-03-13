package com.hyogasa.hgs2.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class abratio {
    private String ratio_date;
    private String ratio_event;
    private String ratio_market;
    private String ratio_chkvol;
    private String ratio_bid;
    private String ratio_bidvol;
    private String ratio_ask;
    private String ratio_askvol;
    private String cre_date;
}
