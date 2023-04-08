package com.hyogasa.hgs2.dbmd.model.dao;

import com.hyogasa.hgs2.dbmd.model.dto.tradeInfo;
import com.hyogasa.hgs2.model.dto.abratio;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
@Mapper
public interface DbMapper {
    List<tradeInfo> getInfoList();
    void insertData(Map<String, String> map);
}
