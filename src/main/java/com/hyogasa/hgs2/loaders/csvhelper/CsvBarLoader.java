package com.hyogasa.hgs2.loaders.csvhelper;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBar;
import org.ta4j.core.BaseBarSeries;

import java.io.*;
import java.nio.file.StandardCopyOption;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.*;
import java.util.logging.Logger;
import java.util.*;


import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class CsvBarLoader {

    private static final Logger LOG = Logger.getLogger(CsvBarLoader.class.getName());

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");

    public static void persistSeries(BarSeries series, String filename, String cn) {
        CsvBarSeries exportableSeries = CsvBarSeries.from(series, cn);

        BufferedWriter bw = null;

        /** 폴더 생성 시작 */
        Path path = Paths.get("src/main/resources/coin/" + cn + "/");

        // 폴더가 없으면
        if(!Files.exists(path)) {
            // 폴더 생성
            try {
                Files.createDirectory(path);
            }catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
        /** 폴더 생성 종료 */

        /** 파일, 데이터 생성 시작 */
        try {
            // 2023-03-09 파일 명 앞에 일자 없애기 일자 변하면서 데이터가 정확해지지 않음.
            //File ff = new File("src/main/resources/coin/" + cn + "/" + filename + "_" + cn + ".csv");
            String filePath = "src/main/resources/coin/" + cn + "/" + cn + "_trade.csv";

            if(!Files.exists(Path.of(filePath))) {
                // 파일 생성
                try {
                    Files.createFile(Path.of(filePath));
                }catch (IOException ioe) {
                    ioe.printStackTrace();
                }

            }

            File ff = new File(filePath);

            double bytes = ff.length();
            double kilo = bytes / 1024;
            double mega = (bytes / 1024) / 1024;

            if(mega > 3) {
                LOG.info("파일 크기 3메가 초과 mega - " + mega);
                File file = new File(filePath);
                File newFile = new File("src/main/resources/coin/" + cn + "/" + cn + "_trade" + new DateTime().getMillis() + ".csv");

                // StandardCopyOption.REPLACE_EXISTING : 기존에 파일이 있더라도 덮어쓰기
                Files.copy(file.toPath(), newFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

                Path fileData = Paths.get(filePath);
                try {
                    // 파일 데이터 읽기
                    RandomAccessFile accFile = new RandomAccessFile(filePath, "r");
                    // 기존 파일에 덮어쓸 라인 담아두는 변수
                    StringBuffer lastLine = new StringBuffer();
                    // 가장 최근 30000라인만 복사해서 넘기기
                    // 기본적으로 3만 라인은 있어야 비슷하게 지표가 맞는다.
                    // 위에는 틀렸다. 대략 1시간 데이터만 남겨놓으면 될듯하다 15천 라인 이상인 경우에 파일 재생성
                    // 그냥 대략적으로 5천라인 가지고 진행 - 20230323
                    long lineCount = 2000;
                    // 불러온 파일 크기 구하기
                    long fileLength = accFile.length();
                    // 마지막 부터 차례로 조회
                    for(long i=fileLength - 1; i>=0; i--) {
                        accFile.seek(i);
                        // 개행(new line) 찾기
                        char c = (char) accFile.read();
                        // 찾을때마다 카운트 마이너스
                        if(c == '\n') {
                            lineCount--;
                            if(lineCount == 0) {
                                break;
                            }
                        }
                        lastLine.insert(0, c);
                    }

                    // 마지막 문자 제거
                    lastLine.deleteCharAt(lastLine.length() - 1);

                    Files.write(fileData, Collections.singleton(lastLine));
                }catch (IOException ioe) {
                    ioe.printStackTrace();
                }

            }

            // 20230328 부터 시작!!!!!
            // csv 파일의 마지막 로우 가져와서 동일한 '분(minutes)'이면 기존 라인 업데이트(delete - insert)
            /** 새로 받아온 데이터가 이전 시간과 똑같으면(hh:mm) 기존 라인 삭제 후 입력 시작 */
            // 랜덤액세스파일함수로 파일 읽기
            RandomAccessFile raf = new RandomAccessFile(filePath, "rw");

            if(raf.length() != 0 ) {

                // 이전 데이터 담아둘 스트링버퍼
                StringBuffer sb = new StringBuffer();

                // 읽어온 파일의 총 길이
                long fileLength = raf.length();
                // 마지막 라인의 시작점 담기위한 변수
                long end = 0;

                // 파일의 총 길이 - 2 부터 빼가면서 삭제할 라인의 시작위치 구하기
                // 파일 총길이 -2 한 이유는 0은 마지막 빈라인이고, -1은 끝지점의 \'n'이 걸려서 -2부터 시작
                for(long pt = fileLength - 2; pt >= 0; pt--) {
                    // 시작 위치 설정
                    raf.seek(pt);

                    // 해당 위치의 캐릭터 값 구하기
                    char c = (char) raf.read();

                    // 구하다가 개행문자 만나면 종료
                    if(c == '\n') {
                        end = pt;
                        break;
                    }

                    // 각 값 담아두기
                    sb.insert(0, c);
                }

                // 위에서 구한 마지막 라인의 데이터를 ',' 구분자로 분리
                String[] arr = String.valueOf(sb).split(",");

                // 마지막 라인 시간 구하기 1
                SimpleDateFormat sdf = new SimpleDateFormat("YYYY-MM-dd HH:mm");

                // 마지막라인 시간 구하기 2
                Timestamp ts = new Timestamp(Long.parseLong(arr[1]));
                String preDate = sdf.format(ts);

                // 현재시간(*신규 데이터의 시간) 확인
                String nowDate = sdf.format(exportableSeries.ohlc.get("endTime"));

                // 신규 데이터의 시간(hh:mm)이 이전 데이터와 같으면
                if(nowDate.equals(preDate)) {
                    // 이전 라인 담아두는 변수 추가
                    StringBuffer newSb = new StringBuffer(String.valueOf(sb));

                   // 이전 라인 길이만큼 " "로 각 위치 초기화
                    for(int i=0; i<newSb.length(); i++) {
                        newSb = newSb.replace(i, i + 1, " ");
                    }

                    // 초기화한 길이만큼 raf(읽어온 파일 데이터)의 값도 초기화
                    byte[] blankVal = String.valueOf(newSb).getBytes("UTF-8");
                    raf.write(blankVal, 0, blankVal.length);

                    raf.setLength(end + 1);
                }
                /** 새로 받아온 데이터가 이전 시간과 똑같으면(hh:mm) 기존 라인 삭제 후 입력 종료 */

            }
            bw = new BufferedWriter(new FileWriter(ff, true));
            String wData = "";

            wData = exportableSeries.ohlc.get("coinName")
                    + "," + exportableSeries.ohlc.get("endTime")
                    + "," + exportableSeries.ohlc.get("openPrice")
                    + "," + exportableSeries.ohlc.get("highPrice")
                    + "," + exportableSeries.ohlc.get("lowPrice")
                    + "," + exportableSeries.ohlc.get("closePrice")
                    + "," + exportableSeries.ohlc.get("volume")
                    + "," + exportableSeries.ohlc.get("amount");

            bw.write(wData);
            bw.newLine();

            //LOG.info("Bar series '" + exportableSeries.ohlc.get("coinName") + "' successfully saved to '" + filename + "'");
        } catch (IOException e) {
            e.printStackTrace();
            //LOG.log(Level.SEVERE, "Unable to store bars in JSON", e);
        } finally {
            if (bw != null) {
                try {
                    bw.flush();
                    bw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        /** 파일, 데이터 생성 종료 */
    }

    public static BarSeries loadSeries(String cn, int duration) {
        // 2023-03-09 파일 명 앞에 일자 없애기 일자 변하면서 데이터가 정확해지지 않음.
        //String fileName = "src/main/resources/coin/" + cn + "/" + td + "_" + cn + ".csv";
        String fileName = "src/main/resources/coin/" + cn + "/" + cn + "_trade.csv";
        //InputStream is = CsvBarLoader.class.getClassLoader().getResourceAsStream(fileName);
        CSVReader csvReader = null;
        List<String[]> lines = null;

        BufferedReader br = null;

        try {
            //LOG.info("시작 - " + LocalTime.now());
            csvReader = new CSVReader(new FileReader(fileName));
            lines = csvReader.readAll();
            //LOG.info("종료 - " + LocalTime.now());
            //LOG.info("사이즈 - " + lines.get(lines.size() - 1));
            //System.out.println(lines);
        }catch (Exception e) {
            //Logger.getLogger(CsvBarLoader.class.getName()).log(Level.SEVERE, "Unable to load trades from CSV", e);
        }finally {
            if(csvReader != null) {
                try{
                    csvReader.close();
                }catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }
        }

        BarSeries series = new BaseBarSeries();

        if((lines != null) && !lines.isEmpty()) {
            ZonedDateTime beginTime = ZonedDateTime
                    .ofInstant(Instant.ofEpochMilli(Long.parseLong(lines.get(0)[1])), ZoneId.systemDefault());

            ZonedDateTime endTime = ZonedDateTime.ofInstant(
                    Instant.ofEpochMilli(Long.parseLong(lines.get(lines.size() - 1)[1])),
                    ZoneId.systemDefault());

            if (beginTime.isAfter(endTime)) {
                Instant beginInstant = beginTime.toInstant();
                Instant endInstant = endTime.toInstant();
                beginTime = ZonedDateTime.ofInstant(endInstant, ZoneId.systemDefault());
                endTime = ZonedDateTime.ofInstant(beginInstant, ZoneId.systemDefault());

                // Since the CSV file has the most recent trades at the top of the file, we'll
                // reverse the list to feed
                // the List<Bar> correctly.
                Collections.reverse(lines);
            }

            // build the list of populated bars
            buildSeries(series, beginTime, endTime, duration, lines);
        }

        return series;
    }


    /**
     * Builds a list of populated bars from csv data.
     *
     * @param beginTime the begin time of the whole period
     * @param endTime   the end time of the whole period
     * @param duration  the bar duration (in seconds)
     * @param lines     the csv data returned by CSVReader.readAll()
     */
    @SuppressWarnings("deprecation")
    private static void buildSeries(BarSeries series, ZonedDateTime beginTime, ZonedDateTime endTime, int duration, List<String[]> lines) {
        //LOG.info("BuildSeries 시작- " + LocalTime.now());
        Duration barDuration = Duration.ofSeconds(duration);
        ZonedDateTime barEndTime = beginTime;
        // line number of trade data
        int i = 0;

        do {
            // build a bar
            barEndTime = barEndTime.plus(barDuration);
            Bar bar = new BaseBar(barDuration, barEndTime, series.function());
            do {
                // get a trade
                String[] tradeLine = lines.get(i);
                ZonedDateTime tradeTimeStamp = ZonedDateTime
                        .ofInstant(Instant.ofEpochMilli(Long.parseLong(tradeLine[1])), ZoneId.systemDefault());
                // if the trade happened during the bar

                if (bar.inPeriod(tradeTimeStamp)) {
                    // add the trade to the bar
                    double tradePrice = Double.parseDouble(tradeLine[5]);
                    double tradeVolume = Double.parseDouble(tradeLine[6]);
                    bar.addTrade(tradeVolume, tradePrice, series.function());
                } else {
                    // the trade happened after the end of the bar
                    // go to the next bar but stay with the same trade (don't increment i)
                    // this break will drop us after the inner "while", skipping the increment
                    break;
                }
                i++;
            } while (i < lines.size());

            // if the bar has any trades add it to the bars list
            // this is where the break drops to
            if (bar.getTrades() > 0) {
                series.addBar(bar);
            }
        } while (barEndTime.isBefore(endTime));

        //LOG.info("BuildSeries 종료- " + LocalTime.now());
    }
}
