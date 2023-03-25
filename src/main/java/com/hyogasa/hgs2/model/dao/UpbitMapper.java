package com.hyogasa.hgs2.model.dao;

import com.hyogasa.hgs2.model.dto.abratio;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
@Mapper
public interface UpbitMapper {
    List<abratio> getAbratioList();

    // 이전 캔들 대비 거래량 조회
    List<Map<String, Object>> getPreCandle();

    void insertData(Map<String, String> map);
}
