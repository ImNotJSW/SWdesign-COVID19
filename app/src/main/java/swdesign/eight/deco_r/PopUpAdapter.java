package swdesign.eight.deco_r;

import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

public class PopUpAdapter implements GoogleMap.InfoWindowAdapter{

    View window;
    String info;
    public PopUpAdapter(View window, String info){
        this.window = window;
        this.info = info;//정보를 담은 객체
    }

    @Override
    public View getInfoWindow(Marker marker) {
        String info2 = marker.getSnippet();
        TextView infoTextView = window.findViewById(R.id.infoTextView);
        infoTextView.setText(info2);
        return window;
    }

    @Override
    public View getInfoContents(Marker marker) {
        return null;
    }
}
