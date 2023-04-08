package com.hyogasa.hgs2.dbmd.service;

import com.hyogasa.hgs2.dbmd.model.dao.DbMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;

@Service
@RequiredArgsConstructor
public class DbService {

    private final DbMapper dbMapper;


    public void insertData(HashMap<String, String> params) {
        System.out.println("테스트33333");
        dbMapper.insertData(params);
    }
}
