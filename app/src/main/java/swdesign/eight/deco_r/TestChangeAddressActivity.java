package swdesign.eight.deco_r;

import androidx.appcompat.app.AppCompatActivity;

import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class TestChangeAddressActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_changeaddress_activity);

        final Geocoder geocoder = new Geocoder(this);
        final Button changeButton = findViewById(R.id.changeButton);
        final EditText addrEditText = findViewById(R.id.addrEditText);
        final TextView resultTextView = findViewById(R.id.resultTextView);

        changeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               String addr = addrEditText.getText().toString();
               ChangerAddress changerAddress = new ChangerAddress(geocoder);

               Location location = changerAddress.changeToLocation(addr);
               if(location != null) {
                   double lat = location.getLatitude();
                   double lon = location.getLongitude();
                   String result = "위도: "+ lat + ", 경도: " + lon;
                   resultTextView.setText(result);
               }
               else {
                   resultTextView.setText("오류");
               }
            }
        });

    }
}
