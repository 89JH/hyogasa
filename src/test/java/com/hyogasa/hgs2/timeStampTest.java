package com.hyogasa.hgs2;

import java.text.SimpleDateFormat;
import java.util.Date;

public class timeStampTest {

    public static void main(String[] args) {
        SimpleDateFormat sdf = new SimpleDateFormat ("yyyy-MM-dd HH:mm:ss"); // [Date 형식으로 변경 : 24 시간 포맷 형태]
        String nowDate = "1676811315764";

        Date date = new Date(Long.parseLong(nowDate));

        System.out.println(sdf.format(date));


    }
}
