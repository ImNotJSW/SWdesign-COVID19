package swdesign.eight.deco_r;

import android.location.Location;

import java.util.ArrayList;

public class DistanceCalculator {
//    private ArrayList<Location> pinLocations;
//    private Location userLocation;
//    private double circleRadius; //미터 단위
    boolean entered = false;

//    public DistanceCalculator() {
//        pinLocations = new ArrayList<Location>();
//        userLocation = new Location("user");
//    }

    //원 반경 안에 들어온 핀 있으면 true, 아니면 false;
    public boolean compareLocation(ArrayList<Location> pinLocations, Location userLocation, double circleRadius) {

        //원반경 내에 들어온 pin 있는지 검사
        for (int i = 0; i < pinLocations.size(); i++) {
            Location pinLocation = pinLocations.get(i);
            double distance = userLocation.distanceTo(pinLocation);
            if (distance < circleRadius) {
                entered = true;
                return entered;
            }
        }
        entered = false;
        return entered;
    }
}
