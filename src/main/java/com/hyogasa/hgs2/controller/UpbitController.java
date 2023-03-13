package com.hyogasa.hgs2.controller;

import com.hyogasa.hgs2.model.dto.abratio;
import com.hyogasa.hgs2.service.UpbitService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
public class UpbitController {
    private final UpbitService upbitService;

    @GetMapping("/hello")
    public String hello() {
        return "Hello world!";
    }

    @GetMapping("/test")
    public List<abratio> getAbratioList() {
        return upbitService.getAbratioList();
    }
}
