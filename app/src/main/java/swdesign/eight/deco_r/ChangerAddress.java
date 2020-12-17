package swdesign.eight.deco_r;

import android.app.Activity;
//import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.util.List;

public class ChangerAddress {
    private Geocoder geocoder;

//    //이 생성자는 무조건 ChangerAddress 클래스가 Activity를 상속받아야 함
//    public ChangerAddress() {
//        geocoder = new Geocoder(this);
//    }

    //이 생성자는 ChangerAddress 클래스가 Activity 상속 안받아도 됨. 단 생성자 인자로 geocoder 받아야 됨
    public ChangerAddress(Geocoder geocoder) {
        this.geocoder = geocoder;
    }

    //인자로 받은 주소 하나(String클래스)를 좌표 하나(Location클래스)로 변환 후 반환하는 함수
    public Location changeToLocation(String str) {
        List<Address> list = null;
        try {
            list = geocoder.getFromLocationName(str,1); //해당 주소를 좌표로 변환해서 저장
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("test", "입출력 오류 - 주소변환시 에러발생");
        }

        if (list != null) {
            if (list.size() == 0) { //이 주소에 해당하는 좌표가 없으면 null 반환
                return null;
            }
            else {
                //좌표의 위경도 구하기
                Address addr = list.get(0);
                double latitude = addr.getLatitude();
                double longitude = addr.getLongitude();

                //위경도를 Location클래스에 저장해 반환
                Location location = new Location("point");
                location.setLatitude(latitude);
                location.setLongitude(longitude);

                return location;

                //아래는 구글맵이 있는 activity에 좌표로 변환한거 넘겨주는 로직
//                String sss = String.format("geo:%f,%f", lat, lon);
//                Intent intent = new Intent(
//                        Intent.ACTION_VIEW,
//                        Uri.parse(sss));
//                startActivity(intent);
            }
        }
        else { //list가 null일시 null 반환
            return null;
        }
    }
}
