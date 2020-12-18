package swdesign.eight.deco_r;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.Manifest;
import android.os.Looper;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, ActivityCompat.OnRequestPermissionsResultCallback {
    //needed Permissions
    final static String[] REQUIRED_PERMISSIONS = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };
    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
    private static final int LOCATION_PERMISSIONS_REQUEST_CODE = 100;
    //public static int circleSize = 2;


    //map (logical)
    GoogleMap map;
    Marker currentMarker = null;
    Circle currentCircle = null;

    Thread crawlingThread;
    ArrayList<ConfirmedData> confirmedDataList;
    ArrayList<Marker> currentMarkers = null;
    ArrayList<Location> pinLocations = null;

    //entire layouts
    private View mLayout;

    Location mCurrentLocation;
    LatLng currentPosition = null;

    FusedLocationProviderClient mFusedLocationClient;
    LocationRequest locationRequest;//location Request Class
    Location location;
    boolean currentMoved = false;

    //Setting 값
    SharedPreferences settingValueStorage;
    final static String storageKey = "lslelxl";
    int alarmType;
    double circleSize;
    int updateIntervalHour;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mLayout = findViewById(R.id.layout_main);

        //저장된 셋팅값을 불러옵니다.
        settingValueStorage = getSharedPreferences(storageKey, MODE_PRIVATE);
        alarmType = settingValueStorage.getInt("alarmType", 3/*기본값 : 소리*/);
        circleSize = settingValueStorage.getFloat("circleSize", 300.0f);
        updateIntervalHour = settingValueStorage.getInt("updateInterval", 12);

        //세팅 버튼에 대한 클릭 리스너 정의
        Button settingBtn = findViewById(R.id.settingBtn);
        settingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, SettingActivity.class);
                intent.putExtra("alarmType", alarmType);
                intent.putExtra("circleSize", circleSize);
                intent.putExtra("updateInterval", updateIntervalHour);
                startActivityForResult(intent, 101);
            }
        });

        //크롤러로부터 확진장소 데이터를 받습니다.
        Crawler.updateIntervalHour = updateIntervalHour; //(크롤러 클래스에 직접 갱신 시간 전달)
        Crawler coronaCrawler = new Crawler(this);
        confirmedDataList = coronaCrawler.getConfirmedDataList();
        crawlingThread = coronaCrawler.crawlingThread;
        //주의 : crawlingThread.join() 구문 이전에 이 ArrayList를 사용하면 에러가 발생함


        // LocationRequest Class setting : 내 위치 정보를 요청하는 객체를 정의합니다.
        locationRequest = new LocationRequest()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(2000)   /*location Request coolTime(ms)*/
                .setFastestInterval(1000); /*fastest Request coolTime(ms)*/

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(locationRequest);


        // map UI and Class : setup and initialize
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        try { //onMapReady Method를 실행하기 전에, crawling 작업을 끝내야 onMapRead Method에서 핀 표시 가능.
            crawlingThread.join(); //confirmedDataList
        } catch (Exception e) {
            Log.e("Thread join", "Thread join failed");
        }

        mapFragment.getMapAsync(this);
        //-------onMapReady Method (콜백)으로 호출됨-------


        //- 테스트 전용 코드 : 앱 화면 하단에 크롤링 결과를 표시
        String str = new String();
        for (ConfirmedData data : coronaCrawler.confirmedDataList) {
            str += data.toString() + "\n";
        }

        TextView textView = findViewById(R.id.text);
        textView.setMovementMethod(new ScrollingMovementMethod());
        textView.setText(str);

    }


    @Override
    public void onMapReady(final GoogleMap googleMap) {
        //Oncreate로 부터 실행됨
        Log.d("Googlemap", "onMapReady :");

        map = googleMap;

        //런타임 퍼미션 요청 대화상자나 GPS 활성 요청 대화상자 보이기전에
        //일단 초기 위치를 대구광역시청으로 둡니다.
        setDefaultLocation();

        //퍼미션 관련 이슈를 조회합니다.
        if (checkLocationPermissionPermitted() == false)
            //장소 권한이 없다면 권한을 요청하고
            requestLocationPermission();
        else {
            //권한이 있다면 바로 현재 위치를 갱신하는 Looper를 실행하는 메소드와, 현재 위치를 표시하는 UI를 호출합니다.
            //(권한이 없는 경우에도 권한을 승락할 경우, onRequestPermissionResult에서 아래 두 함수 모두 호출해줌)
            startLocationUpdates();
            map.setMyLocationEnabled(true);
        }


        map.getUiSettings().setMyLocationButtonEnabled(true);


        //확대 비율 조정 (오동작 위험 있음)
        map.animateCamera(CameraUpdateFactory.zoomTo(15));


        //- 전달받은 방문장소 데이터로 지도에 핀을 표시하는 메소드를 호출합니다.
        updatePin(confirmedDataList);
    }

    //----------------------------onMapReady의 메소드 실행 순서로 배치---------------------------------

    public void setDefaultLocation() {


        //디폴트 위치, 대구광역시청
        LatLng DEFAULT_LOCATION = new LatLng(35.8713, 128.6017);
//        String markerTitle = "위치정보 가져올 수 없음";
//        String markerSnippet = "위치 퍼미션과 GPS 활성 여부 확인하세요";
//
//
//        if (currentMarker != null) currentMarker.remove();
//
//        MarkerOptions markerOptions = new MarkerOptions();
//        markerOptions.position(DEFAULT_LOCATION);
//        markerOptions.title(markerTitle);
//        markerOptions.snippet(markerSnippet);
//        markerOptions.draggable(true);
//        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
//        currentMarker = map.addMarker(markerOptions);

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(DEFAULT_LOCATION, 15);
        map.moveCamera(cameraUpdate);

    }

    private void requestLocationPermission() {
        //1. 사용자가 퍼미션 거부를 한 적이 있는 경우에는
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0])) {

            //2. 요청을 진행하기 전에 사용자가에게 퍼미션이 필요한 이유를 설명해줄 필요가 있습니다.
            Snackbar.make(mLayout, "이 앱을 실행하려면 위치 접근 권한이 필요합니다.",
                    Snackbar.LENGTH_INDEFINITE).setAction("확인", new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    //3. 사용자에 퍼미션 요청을 합니다. 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                    ActivityCompat.requestPermissions(MainActivity.this, REQUIRED_PERMISSIONS, LOCATION_PERMISSIONS_REQUEST_CODE);
                }
            }).show();


        } else {
            //4. 사용자가 퍼미션 거부를 한 적이 없는 경우에는 퍼미션 요청을 바로 합니다.
            // 요청 결과는 onRequestPermissionResult에서 수신됩니다.
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, LOCATION_PERMISSIONS_REQUEST_CODE);
        }
    }

    private void startLocationUpdates() {

        //먼저, GPS가 실행되어 있는지 확인합니다.
        if (checkLocationServicesStatus() == false) {

            Log.d("GPSUpdate", "startLocationUpdates : call showDialogForLocationServiceSetting");
            showDialogForLocationServiceSetting();
        } else {

            if (checkLocationPermissionPermitted() == false) {
                Log.d("GPSUpdate", "startLocationUpdates : 퍼미션 안가지고 있음");
                return;
            }


            Log.d("GPSUpdate", "startLocationUpdates : call mFusedLocationClient.requestLocationUpdates");

            //현재 위치를 지속적으로 갱신하기 위한 Looper를 설정합니다.
            mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());

        }

    }

    private void showDialogForLocationServiceSetting() {

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("위치 서비스 비활성화");
        builder.setMessage("앱을 사용하기 위해서는 위치 서비스가 필요합니다.\n"
                + "위치 설정을 수정하실래요?");
        builder.setCancelable(true);
        builder.setPositiveButton("설정", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                Intent callGPSSettingIntent
                        = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(callGPSSettingIntent, GPS_ENABLE_REQUEST_CODE);
            }
        });
        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        builder.create().show();
    }

    LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            //이 콜백은 startLocationUpdates 메소드 내의 Looper에 의해 반복됨
            super.onLocationResult(locationResult);
            List<Location> locationList = locationResult.getLocations();
            if (locationList.size() > 0) {
                location = locationList.get(locationList.size() - 1);
                //location = locationList.get(0);

                currentPosition = new LatLng(location.getLatitude(), location.getLongitude());


                //String markerTitle = getCurrentAddress(currentPosition);
                String markerSnippet = "위도:" + String.valueOf(location.getLatitude()) + " 경도:" + String.valueOf(location.getLongitude());
                Log.d("locationCallback - ", "onLocationResult : " + markerSnippet);


                //단 한번만 현재 위치로 카메라 자동 이동
                if (currentMoved == false) {
                    Log.d("locationCallback - ", "move To My Position");
                    CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLng(currentPosition);
                    map.moveCamera(cameraUpdate);
                    currentMoved = true;
                }

                //현재 위치에 반경원 생성하고 이동
                updateCircle(location);

                mCurrentLocation = location;

            }

        }
    };


    //-----------------------------onMapReady의 메소드 실행 순서로 배치 End----------------------------

    @Override
    protected void onStart() {
        super.onStart();

        Log.d("onStart - ", "onStart");

        //if (checkPermission()) {

        Log.d("onStart - ", "onStart : call mFusedLocationClient.requestLocationUpdates");
        mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);

        if (map != null) {
            map.setMyLocationEnabled(true);
        }


    }

    @Override
    protected void onStop() {

        super.onStop();

        if (mFusedLocationClient != null) {

            Log.d("onStop", "onStop : call stopLocationUpdates");
            mFusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }


    //마커 생성?
//    public void setCurrentLocation(Location location, String markerTitle, String markerSnippet) {
//
//
//        if (currentMarker != null) currentMarker.remove();
//
//
//        LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
//
//        MarkerOptions markerOptions = new MarkerOptions();
//        markerOptions.position(currentLatLng);
//        markerOptions.title(markerTitle);
//        markerOptions.snippet(markerSnippet);
//        markerOptions.draggable(true);
//
//
//        currentMarker = map.addMarker(markerOptions);
//
//        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLng(currentLatLng);
//        map.moveCamera(cameraUpdate);
//
//    }

    //반경원 생성 및 갱신
    public void updateCircle(Location location) {
        if (currentCircle != null) currentCircle.remove(); //기존 반경원 삭제

        LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude()); //현재 위치 구해서 변수에 저장

        // 반경 원
        CircleOptions circleOptions = new CircleOptions().center(currentLatLng) //원점
                .radius(circleSize)      //반지름 단위 : m(나중: 나중에 설정 클래스의 get으로 이 반지름 받아야됨)
                .strokeWidth(0f)  //선너비 0f : 선없음
                .fillColor(Color.parseColor("#55FE9A2E")); //배경색

        //현재 반경원 저장
        currentCircle = map.addCircle(circleOptions);

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLng(currentLatLng);
        map.moveCamera(cameraUpdate);
    }

    //핀들 지도에 찍는 함수 + pinLocations 리스트 생성 및 그 리스트에 객체들 삽입
    public void updatePin(ArrayList<ConfirmedData> pinInfos) {
        if (currentMarkers != null) { //기존에 있던 pin들 다 제거
            for (int i = 0; i < currentMarkers.size(); i++) {
                currentMarkers.get(i).remove();
            }
            currentMarkers.clear();
        } else {
            currentMarkers = new ArrayList<Marker>();
        }

        if (pinLocations == null) {
            pinLocations = new ArrayList<Location>();
        }

        ConfirmedData pinInfo;
        Location pinLocation;
        ChangerAddress changerAddress = new ChangerAddress(new Geocoder(this));
        //반복문 돌리면서 pin마다 지도에 추가
        for (int i = 0; i < pinInfos.size(); i++) {
            pinInfo = pinInfos.get(i); //핀 하나 받음

            //pin하나의 위경도 정보 받고, 그거를 pinLocations 리스트에 삽입
            pinLocation = changerAddress.changeToLocation(pinInfo.address);
            pinLocations.add(pinLocation);

            //지도에 찍을 pin에 필요한 정보들 넣기
            LatLng pinLatLng = new LatLng(pinLocation.getLatitude(), pinLocation.getLongitude());
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(pinLatLng);
            markerOptions.title(pinInfo.address);
            markerOptions.snippet(pinInfo.toString());
            markerOptions.draggable(true);

            //marker를 실제로 지도에 찍고, 그 marker를 currrentMarkers 리스트에 삽입
            currentMarker = map.addMarker(markerOptions);
            currentMarkers.add(currentMarker);
        }
//        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLng(currentLatLng);
//        map.moveCamera(cameraUpdate);

    }

    //좌표->주소 변환
    public String getCurrentAddress(LatLng latlng) {

        //지오코더... GPS를 주소로 변환
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        List<Address> addresses;

        try {

            addresses = geocoder.getFromLocation(
                    latlng.latitude,
                    latlng.longitude,
                    1);
        } catch (IOException ioException) {
            //네트워크 문제
            Toast.makeText(this, "지오코더 서비스 사용불가", Toast.LENGTH_LONG).show();
            return "지오코더 서비스 사용불가";
        } catch (IllegalArgumentException illegalArgumentException) {
            Toast.makeText(this, "잘못된 GPS 좌표", Toast.LENGTH_LONG).show();
            return "잘못된 GPS 좌표";

        }


        if (addresses == null || addresses.size() == 0) {
            Toast.makeText(this, "주소 미발견", Toast.LENGTH_LONG).show();
            return "주소 미발견";

        } else {
            Address address = addresses.get(0);
            return address.getAddressLine(0).toString();
        }

    }

    //--단순 Boolean
    public boolean checkLocationServicesStatus() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    private boolean checkLocationPermissionPermitted() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }


    //--결과 메소드들
    //세팅창에서, 정상적인 값을 입력하여 설정값이 바뀌는 경우 아래 메소드가 호출됨
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GPS_ENABLE_REQUEST_CODE) {
            //사용자가 GPS 활성 시켰는지 검사
            if (checkLocationServicesStatus()) {
                Log.d("onActivityResult-", "onActivityResult : GPS 활성화 되있음");

                return;
            }
        }

        if (resultCode == 101) {
            if (data.getIntExtra("updateInterval", 0) == 0)
                //설정 엑티비티에서 설정 변경이 없던 경우
                return;
            else {
                //설정 엑티비티에서 설정 변경이 된 경우 : 설정 엑티비티에서 저장된 값을 불러와 적용
                alarmType = data.getIntExtra("alarmType", 3);
                circleSize = data.getDoubleExtra("circleSize", 300.0);
                updateIntervalHour = data.getIntExtra("updateInterval", 12);
                //+ 변경된 설정 값을 디스크에 저장하기.
                SharedPreferences.Editor settingEditor = settingValueStorage.edit();
                settingEditor.putInt("alarmType", alarmType);
                settingEditor.putFloat("circleSize", (float) circleSize);
                settingEditor.putInt("updateInterval", updateIntervalHour);
                settingEditor.commit();

                //테스트용
                //String resultString = alarmType + " " + circleSize + " " + updateIntervalHour;
                //Toast.makeText(this, resultString, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int permsRequestCode, @NonNull String[] permissions, @NonNull int[] grandResults) {

        if (permsRequestCode == LOCATION_PERMISSIONS_REQUEST_CODE) {

            // 요청 코드가 LOCATION_PERMISSIONS_REQUEST_CODE 이면

            boolean check_result = checkLocationPermissionPermitted();


            if (check_result == false)
                // 거부한 퍼미션이 있다면 앱을 사용할 수 없는 이유를 설명해주고 앱을 종료합니다.2 가지 경우가 있습니다.

                if (ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0])
                        || ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[1])) {


                    // 사용자가 거부만 선택한 경우에는 앱을 다시 실행하여 허용을 선택하면 앱을 사용할 수 있습니다.
                    Snackbar.make(mLayout, "퍼미션이 거부되었습니다. 앱을 다시 실행하여 퍼미션을 허용해주세요. ",
                            Snackbar.LENGTH_INDEFINITE).setAction("확인", new View.OnClickListener() {

                        @Override
                        public void onClick(View view) {

                            finish();
                        }
                    }).show();

                } else {

                    // "다시 묻지 않음"을 사용자가 체크하고 거부를 선택한 경우에는 설정(앱 정보)에서 퍼미션을 허용해야 앱을 사용할 수 있습니다.
                    Snackbar.make(mLayout, "퍼미션이 거부되었습니다. 설정(앱 정보)에서 퍼미션을 허용해야 합니다. ", Snackbar.LENGTH_INDEFINITE).setAction("확인", new View.OnClickListener() {

                        @Override
                        public void onClick(View view) {

                            finish();
                        }
                    }).show();
                }
            else {
                startLocationUpdates();
                map.setMyLocationEnabled(true);
                currentMoved = false;
            }
        }
    }




}

