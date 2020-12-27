package swdesign.eight.deco_r;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Geocoder;
import android.location.Location;
import android.util.Log;

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.Segment;
import net.htmlparser.jericho.Source;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

public class AppMainData {

    //[1] Setting Field
    public static SharedPreferences settingStorage = null; //Setting storage Caller
    public final static String settingStorageKey = "SSK";

    public static int alarmType;
    public final static int MUTE = 1, VIBRATION = 2, SOUND = 3;
    public static double circleSize;
    public static int updateIntervalHour;


    //[2] Crawling/ConfirmedData set Field
    public static SharedPreferences confirmedDataStorage = null;
    public final static String confirmedDataStorageKey = "CDSK";

    public final static String urlPath = "http://covid19.daegu.go.kr/00937400.html";
    public static LinkedList<ConfirmedData> confirmedDataList;     //[First] newData --- oldData [Last]
    private static Thread storageThread;







    //[1] -> get SettingValue
    public static void loadSettingData(Context context) {
        if (settingStorage == null) {
            settingStorage = context.getSharedPreferences(settingStorageKey, MODE_PRIVATE);
        }
        alarmType = settingStorage.getInt("alarmType", SOUND);
        circleSize = settingStorage.getFloat("circleSize", 300.0f);
        updateIntervalHour = settingStorage.getInt("updateInterval", 12);
    }



    //[2] -> get ConfirmedData
    //Dependency : 먼저 loadSettingData 함수를 호출해야함
    //return : 데이터 셋을 생성하고 있는 쓰레드를 반환함
    public static Thread loadConfirmedDataSet(Context context) {
        if (confirmedDataStorage == null)
            confirmedDataStorage = context.getSharedPreferences(confirmedDataStorageKey, MODE_PRIVATE);

        //1. 최근 갱신 후 경과 시간 체크
        long passedTime = System.currentTimeMillis() - confirmedDataStorage.getLong("updateTime", 0);   //Unit : millisecond
        int passedHour = (int)passedTime / 1000 / 60 / 60;                                                            //경과 시간 3년 이내면 강제 형변환에 의한 Overflow 발생 위험 없음

        //2. 데이터가 없거나 오래되었다면, 웹 사이트에서 코로나 정보를 가져옴
        //   그렇지 않다면 그냥 기존 데이터를 불러옴 (두 경우 모두 쓰레드 활용)
        Thread callingThread;
        confirmedDataList = new LinkedList<ConfirmedData>();

        //passedHour = 1000;
        if (passedHour != updateIntervalHour) {
            Log.d("dataLoad result - ", "Web crawling Again!");
            callingThread = callWebData(context);
        } else {
            Log.d("dataLoad result - ", "Use just Stored Data.");
            callingThread = callStorageData();
        }
        return callingThread;
    }



    //loadConfirmedDataSet 파생 메소드 : 새 쓰레드에 웹 크롤링을 수행하고 쓰레드 리턴
    public static Thread callWebData(final Context context) {
        //webThread(웹 크롤링) -> storageThread(크롤링한 데이터 저장) 순으로 동기화하고
        //webThread만 리턴 (storageThread는 main Thread와의 의존성 없음)



        final Thread webThread = new Thread() {
            @Override
            public void run() {

                    Source source;
                    ChangerAddress changerAddress = new ChangerAddress(new Geocoder(context));

                    try {
                        URL coronaUrl = new URL(urlPath);
                        source = new Source(coronaUrl);
                    } catch (MalformedURLException e) {
                        Log.e("Crawler Init : ", "Malformed URL Error");
                        return;
                    } catch (IOException e) {
                        Log.e("Crawler Init : ", "Web Connection Error");
                        return;
                    }


                    //해당 부분부터는 대구 코로나 웹사이트 소스코드에 대한 파싱 알고리즘입니다.
                    //1. 웹 소스코드를 행 단위로 쪼개어 리스트로 저장
                    List<Element> rowList = source.getFirstElement("tbody").getAllElements("tr");
                    rowList = rowList.subList(2, rowList.size());


                    for (Element row : rowList) {
                        //2. 각 행마다 반복하여 다시 열로 쪼개어 테이플 한칸을 여러 개 담는 리스트로 저장
                        Segment rowParser = row.getContent();
                        List<Element> columnElementList = rowParser.getAllElements("td");


                        ConfirmedData lineData = null;
                        int columnIndex = 0;
                        final int INVALID_LINE = -99;

                        //element는 확진자 정보 표의 한 칸. (표를 Z 방향으로 읽는다고 보면 됨)
                        for (Element element : columnElementList) {
                            String elementStr = element.getContent().toString();
                            //<br>은 HTML에서 개행을 의미하는데, 공백 문자로 치환
                            //elementStr = elementStr.replace("<br>", " ");
                            //태그와 개행 제거 구문
                            elementStr = elementStr.replaceAll("<(/)?([a-zA-Z]*)(\\s[a-zA-Z]*=[^>]*)?(\\s)*(/)?>", "");
                            elementStr = elementStr.replaceAll("\n", "");

                            //3. 각 칸의 문자열을 ConfirmedData 클래스의 변수에 집어넣고 List에 추가
                            switch (columnIndex) {
                                case 0:  //(0열)시도 열인 케이스
                                    lineData = new ConfirmedData();
                                    lineData.setAddress(elementStr);
                                    break;

                                case 1: //(1열) 시군구 열
                                    //-를 제외한 특수 문자와 영단어 제거 (주소는 한글/숫자/- 만 있어야 합니다)
                                    lineData.setAddress(elementStr.replaceAll("[a-zA-Z!-,./]", ""));
                                    break;

                                case 2: //(2열) 장소유형 열
                                    lineData.setPlaceType(elementStr);
                                    //대중교통 기록의 경우 확진자 정보로 추가하지 않음. - 해당 행은 무시
                                    if (elementStr.equals("대중교통") == true) {
                                        columnIndex = INVALID_LINE;
                                        break;
                                    }
                                    break;

                                case 3: //상호명 열
                                    lineData.setName(elementStr);
                                    break;

                                case 4: //(4열) 주소 열
                                    final String address = //(도로명 주소까지 완료된 상태)
                                            (lineData.getAddress() + " " + elementStr).replaceAll("[a-zA-Z!-,./]", "");
                                    //확진장소 좌표 조회 및 저장
                                    Location pinCoordinate = changerAddress.changeToLocation(address);
                                    lineData.setLongitude(pinCoordinate.getLongitude());
                                    lineData.setLatitude(pinCoordinate.getLatitude());
                                    lineData.setAddress(address);
                                    break;

                                case 5: //노출일시 열
                                    lineData.setDateOfVisit(lineData.getDateOfVisit() + elementStr);
                                    break;

                                case 6: //소독여부 열
                                    lineData.setDisinfection(elementStr.contains("소독완료"));
                                    columnIndex = -1;
                                    confirmedDataList.addLast(lineData); //시간복잡도 : O(1)


                                    Log.d("updateList", "One Line Added : " + lineData.getName());
                                    break;

                            }

                            if (columnIndex == INVALID_LINE)
                                break;
                            columnIndex++;
                        }
                    }

            }
        };

        storageThread = new Thread() {
            @Override
            public void run() {

                try {
                    webThread.join(); //webThread가 끝날 때까지 대기한다.
                } catch (Exception e) { }
                SharedPreferences.Editor editor = confirmedDataStorage.edit();
                editor.putLong("updateTime", System.currentTimeMillis()); //갱신 시간 저장

                Iterator<ConfirmedData> lListIterator = confirmedDataList.iterator();
                int index = 0;
                while (lListIterator.hasNext()) {
                    String key = "row" + index;
                    ConfirmedData row = lListIterator.next();
                    editor.putString(key, row.getAddress() + "|" + row.getPlaceType() + "|" + row.getName() + "|" + row.getDateOfVisit() + "|" + row.isDisinfection() + "|"
                                            + row.getLongitude() + "|" + row.getLatitude());
                    index++;
                }
                Log.d("rowCount = ", ""+index);
                editor.putInt("rowCount", index); //저장된 확진자 정보의 수도 저장



                editor.commit();
                Log.d("storageThread - ", "Save Finished");
            }
        };

        webThread.setPriority(8);
        webThread.start();
        storageThread.start();

        return webThread;
    }

    //loadConfirmedDataSet 파생 메소드 : 새 쓰레드에 SharedPreference 데이터를 조회하고 쓰레드 리턴
    public static Thread callStorageData() {
        Thread listThread = new Thread() {
            @Override
            public void run() {
                int indexEnd = confirmedDataStorage.getInt("rowCount", 0);

                for (int index = 0; index < indexEnd; index++) {
                    String loadString = confirmedDataStorage.getString("row" + index, null);
                    Log.d("callStorage - ", "loadString = " + loadString);
                    if (loadString == null) {
                        Log.e("callStorage - ", "당초 조회한 데이터보다 적게 저장되었음");
                        break;
                    }
                    String[] parsedStringList = loadString.split("\\|");
                    ConfirmedData loadedData = new ConfirmedData();
                    loadedData.setAddress(parsedStringList[0]);
                    loadedData.setPlaceType(parsedStringList[1]);
                    loadedData.setName(parsedStringList[2]);
                    loadedData.setDateOfVisit(parsedStringList[3]);
                    loadedData.setDisinfection(parsedStringList[4].equals("true"));
                    loadedData.setLongitude(Double.parseDouble(parsedStringList[5]));
                    loadedData.setLatitude(Double.parseDouble(parsedStringList[6]));
                    confirmedDataList.addLast(loadedData);

                }
            }
        };
        listThread.setPriority(8);
        listThread.start();

        return listThread;
    }


}
