package swdesign.eight.deco_r;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import net.htmlparser.jericho.*;



public class Crawler {

    final static String urlPath = "http://covid19.daegu.go.kr/00937400.html";
    final static String tableName = "ConfirmedDataTable";
    public static int updateIntervalHour = 12;
    ArrayList<ConfirmedData> confirmedDataList;
    Context context;
    SharedPreferences confirmedDataStorage;
    public Thread crawlingThread;


    //Crawler 생성자 :
    public Crawler(Context context) {
        this.context = context;
        this.confirmedDataStorage = context.getSharedPreferences(tableName, Context.MODE_PRIVATE);
        this.confirmedDataList = new ArrayList<>(); //확진자 리스트 초기화
    }


    //저장된 정보를 재사용하거나 웹데이터를 크롤링/파싱하여 List에 저장
    public ArrayList<ConfirmedData> getConfirmedDataList() {
        long lastUpdateTime = confirmedDataStorage.getLong("updateTime", 0);

        //테이블이 없거나, 오래되었다면 (시간 갱신 후) 크롤링 수행
        if (dataIsOld(lastUpdateTime) == true) {
            updateConfirmedDataList(); //크롤링 실행 로직은 여기에 다 들어있음
            boolean updateResult = true;
            if (updateResult == false)
                Toast.makeText(context, "코로나 정보 갱신에 실패하였습니다.", Toast.LENGTH_SHORT).show();
            else {
                Toast.makeText(context, "코로나 확진 정보가 갱신되었습니다.", Toast.LENGTH_SHORT).show();
            }

        } else {
            //오래되지 않은 확진자 정보 데이터는 저장된 그대로 가져다 씀
            Toast.makeText(context, "기존 정보를 사용합니다.", Toast.LENGTH_SHORT).show();
            int index = 0;
            while (true) {
                String loadString = confirmedDataStorage.getString("row" + index, null);
                if (loadString == null)
                    break;
                String[] parsedStringList = loadString.split("\\|");
                ConfirmedData loadedData = new ConfirmedData();
                loadedData.setAddress(parsedStringList[0]);
                loadedData.setPlaceType(parsedStringList[1]);
                loadedData.setName(parsedStringList[2]);
                loadedData.setDateOfVisit(parsedStringList[3]);
                loadedData.setDisinfection(parsedStringList[4].equals("true"));


                confirmedDataList.add(loadedData);
                index++;
            }
        }

        return this.confirmedDataList;
    }

    private void updateConfirmedDataList() {
        //update 로직 본문은 updateThreadMethod에 있음.

        crawlingThread = new Thread(new Runnable() {
            @Override
            public void run() {
                updateThreadMethod();
            }
        });
        crawlingThread.start();

    }

    private void updateThreadMethod() {
        Source source;
        try {
            URL coronaUrl = new URL(urlPath);
//            HttpURLConnection con = (HttpURLConnection) coronaUrl.openConnection();
//            con.setDoOutput(true);
//            OutputStream wr = con.getOutputStream();
//            wr.flush();
//
//            InputStream is = con.getInputStream();
//            Reader r = new InputStreamReader(is, "utf-8");
            source = new Source(coronaUrl);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("Crawler Init : ", "Malformed URL text or HTML Error");
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

                    case 1:
                    case 4: //(1열)시군구 열 또는 (4열) 주소 열
                        String address = lineData.getAddress() + " " + elementStr;
                        //-를 제외한 특수 문자와 영단어 제거 (주소는 한글/숫자/- 만 있어야 합니다)
                        lineData.setAddress(address.replaceAll("[a-zA-Z!-,./]", ""));
                        break;

                    case 2: //(2열) 장소유형 열
                        lineData.setPlaceType(elementStr);
                        break;

                    case 3: //상호명 열
                        lineData.setName(elementStr);
                        break;

                    case 5: //노출일시 열
                        lineData.setDateOfVisit(lineData.getDateOfVisit() + elementStr);
                        break;

                    case 6: //소독여부 열
                        lineData.setDisinfection(elementStr.equals("소독완료"));
                        columnIndex = -1;
                        if (lineData.getPlaceType().equals("대중교통") == false) //대중교통 기록은 확진자 정보로 추가하지 않음.
                            confirmedDataList.add(lineData);

                        //System.out.println(lineData);
                        Log.d("updateList", "One Line Added");

                        break;

                }
                columnIndex++;
            }

        }

        //내부 저장 데이터도 새로 갱신된 테이블로 대체함
        SharedPreferences.Editor editor = confirmedDataStorage.edit();
        editor.putLong("updateTime", System.currentTimeMillis()); //갱신 시간 저장
        for (int index = 0; index < confirmedDataList.size(); index++) { //확진자 데이터 저장
            String key = "row" + index;
            final ConfirmedData row = confirmedDataList.get(index);
            // | 문자로 구분하여 DB에 저장
            editor.putString(key, row.getAddress() + "|" + row.getPlaceType() + "|" + row.getName() + "|" + row.getDateOfVisit() + "|" + row.isDisinfection());
        }

        editor.commit();

    }

    private boolean dataIsOld(long lastUpdateTime) {
        long timeInterval = System.currentTimeMillis() - lastUpdateTime;
        long intervalHour = timeInterval / 1000 / 60 / 60;
        return intervalHour >= updateIntervalHour;
    }
}
