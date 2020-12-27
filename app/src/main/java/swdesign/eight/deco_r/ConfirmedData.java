package swdesign.eight.deco_r;

import androidx.annotation.Nullable;

public class ConfirmedData {
    String address;
    String placeType;
    String name;
    String dateOfVisit = "";
    boolean isDisinfection;

    double longitude;  //derived by address
    double latitude;    //derived by address



    public String getAddress() {
        return address;
    }
    public void setAddress(String address) {
        this.address = address;
    }

    public String getPlaceType() {
        return placeType;
    }

    public void setPlaceType(String placeType) {
        this.placeType = placeType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDateOfVisit() {
        return dateOfVisit;
    }

    public void setDateOfVisit(String dateOfVisit) {
        this.dateOfVisit = dateOfVisit;
    }

    public boolean isDisinfection() {
        return isDisinfection;
    }

    public void setDisinfection(boolean disinfection) {
        isDisinfection = disinfection;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longtitude) {
        this.longitude = longtitude;
    }

    public String toString() {
        String boolToKorean = this.isDisinfection ? "예" : "아니오";

        return "주소 : " + this.address +
                "\n장소 유형 : " + this.placeType +
                "\n이름 : " + this.name +
                "\n방문일시 : " + this.dateOfVisit +
                "\n방역여부 : " + boolToKorean + "\n";
    }

    //이 클래스의 동등 정체성은 name으로 결정됨
    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj instanceof ConfirmedData) {
            ConfirmedData typeFitObj = (ConfirmedData)obj;
            return this.name.equals(typeFitObj.getName());
        }
        else
            return super.equals(obj);
    }
}

