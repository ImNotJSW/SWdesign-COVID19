import android.util.Log;

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.Source;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class NormalNoticeInfoBoard extends InfoBoard {


    public NormalNoticeInfoBoard() {
        setRootURLAgain();
        //btin.page=? 부분이 게시판 페이지 값임.
        InfoBoardName = "일반공지";
    }

    @Override
    protected void setRootURLAgain() {
        rootURL = "http://www.knu.ac.kr/wbbs/wbbs/bbs/btin/list.action?bbs_cde=1&btin.page=" + boardPage + "&popupDeco=false&btin.search_type=&btin.search_text=&menu_idx=67";
    }

    //쓰래드를 통해 네트워크를 사용하여 공지 "1페이지"(가변적 수정이 필요한 부분) 정보를 받아옴.
    @Override
    public InfoBoard getInfoPostData() {
        networkThread = new Thread() {
            public void run() {
                try {
                    //java의 IOstream 관련 클래스를 사용해서 사이트의 정보를 따온다.
                    URL url
                            = new URL(rootURL);
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    con.setDoOutput(true);
                    OutputStream wr = con.getOutputStream();
                    wr.flush();

                    InputStream is = con.getInputStream();
                    Reader r = new InputStreamReader(is, "utf-8");
                    Source src = new Source(r); //html 소스가 그대로 담겨있는 src

                    List<Element> data = src.getAllElements("tr");


                    InfoPost temp = null;

                    for (int i = 0; i < data.size(); i++) {
                        List<Element> subData = data.get(i).getAllElements("td");

                        if (subData.size() >= 6) { //아마 맞을 꺼임?
                            boolean validity = true;
                            for (int j = 0; j < subData.size() && validity; j++) {
                                Element e = (Element) subData.get(j);
                                String str = e.getAttributeValue("class");

                                switch (str) {
                                    case "num notice": //게시판 상단의 "공지"로 되어있는 것은 중복되므로 포함하지 않음.
                                        validity = false;
                                        break;
                                    case "num":
                                        temp = new InfoPost();
                                        temp.postResource = InfoBoardName;
                                        temp.bbs_num = e.getTextExtractor().toString();
                                        break;
                                    case "subject":
                                        List<Element> link = data.get(i).getAllElements("a");
                                        Element e2 = (Element) link.get(0);
                                        String URLParameter = e2.getAttributeValue("href");
                                        temp.clickLink = rootURL.split("knu.ac.kr")[0] + "knu.ac.kr/" + URLParameter;
                                        temp.title = e.getTextExtractor().toString();
//                                        for (int k = 0; k < link.size(); k++) {
//                                            Element e2 = (Element) link.get(k);
//                                            String URLParameter = e2.getAttributeValue("href");
//                                            if (URLParameter != null) {
//                                                temp.clickLink = rootURL + URLParameter;
//                                                //nullptr Exception 주의
//                                                break;
//                                            }
//                                        }
                                        break;
                                    case "file":
                                        break; //첨부파일이 있는 지 보여주는 class인데 별로 필요없어서 생략
                                    case "writer":
                                        temp.bbs_writer = e.getTextExtractor().toString();
                                        break;
                                    case "date":
                                        temp.bbs_date.StringToSimpleDate(e.getTextExtractor().toString());
                                        break;
                                    case "hit":
                                        temp.bbs_hit = e.getTextExtractor().toString();
                                        break;
                                    default:
                                        Log.e("Unexpected", "CollageNotice HTML tag Class name");
                                }
                            }
                            //if (validity) Log.d("Soon added postList", temp.toString());
                            if (validity) {
                                Log.d("Now Adding postList : ", temp.toString());
                                postList.add(temp);
                            }
                        }
                    }
                } catch (Exception e) {
                    Log.e("Net", "Error in network call", e);
                    //Print error message at Logcat.
                }
            }
        };
        networkThread.start();

        return this;
    }
}
