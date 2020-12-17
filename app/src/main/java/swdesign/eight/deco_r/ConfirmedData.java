package swdesign.eight.deco_r;

public class ConfirmedData {
    String address;
    String placeType;
    String name;
    String dateOfVisit = "";
    boolean isDisinfection;


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

    public String toString() {
        return "주소 : " + this.address +
                "\n장소 유형 : " + this.placeType +
                "\n이름 : " + this.name +
                "\n방문일시 : " + this.dateOfVisit +
                "\n방역여부 : " + this.isDisinfection + "\n";
    }
}

