package com.hyogasa.hgs2.dbmd.model.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class tradeInfo {
    String credate; // 거래시간
    String op;  // opening price 시가
    String hp;  // high price 고가
    String lp;  // low price 저가
    String tp;  // trade price 거래가
    String vol; // 볼륨
    String tt;// trade type 매수/매도 구분

    @Builder
    public tradeInfo(String credate, String op, String hp, String lp, String tp, String vol, String tt) {
        this.credate = credate;
        this.op = op;
        this.hp = hp;
        this.lp = lp;
        this.tp = tp;
        this.vol = vol;
        this.tt = tt;
    }

}
