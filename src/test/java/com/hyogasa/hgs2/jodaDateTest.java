package com.hyogasa.hgs2;

import com.hyogasa.hgs2.api.Util;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class jodaDateTest {

    public static void main(String[] args) {


        System.out.println("date222- " + Util.convertUtctoLocal("2023-03-02T21:02:03Z"));

        ZonedDateTime date = ZonedDateTime.of(LocalDateTime.parse("2023-03-02T12:35:42"), ZoneId.of("Asia/Seoul"));

        System.out.println("date - " + date.toLocalDateTime());
    }
}
