package br.edu.ifsp.scl.beacons;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.ViewAnimator;

import com.estimote.coresdk.common.config.EstimoteSDK;
import com.estimote.coresdk.common.requirements.SystemRequirementsChecker;
import com.estimote.coresdk.observation.region.beacon.BeaconRegion;
import com.estimote.coresdk.recognition.packets.Beacon;
import com.estimote.coresdk.service.BeaconManager;

import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    private BeaconManager manager;
    private Beacon myBeacon;
    private BeaconRegion region;
    private LinearLayout layoutMint, layoutIcy, layoutBlueberry;

    public static final String APP_ID = "App-ID-from-owner";
    public static final String APP_TOKEN = "App-Token-from-owner";
    public static final String APP_UUID = "App-UUID-from-owner";

    private static final String ID_MINT = "IBEACON-f178173700b2";
    private static final String ID_ICY = "IBEACON-d2ee302bdd7c";
    private static final String ID_BLUEBERRY = "IBEACON-e2700cc77817";

    private int counter = 0;
    private int layoutActivated;
    private String beaconId;
    private Boolean semaphore = false;
    private int shortDistance = 300;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        layoutMint = findViewById(R.id.Mint);
        layoutIcy = findViewById(R.id.Icy);
        layoutBlueberry = findViewById(R.id.blueberry);

        EstimoteSDK.initialize(getApplicationContext(), APP_ID, APP_TOKEN);
        EstimoteSDK.enableDebugLogging(true);

        manager = new BeaconManager(getApplicationContext());
        region = new BeaconRegion("BEACON_DEBUGGER", UUID.fromString(APP_UUID), null, null);

        manager.setRangingListener(new BeaconManager.BeaconRangingListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onBeaconsDiscovered(BeaconRegion beaconRegion, List<Beacon> receivedBeacons) {
                if (receivedBeacons.size() > 0) {
                    shortDistance = -250;

                    for (int i = 0; i < receivedBeacons.size(); i++) {
                        if (receivedBeacons.get(i).getRssi() > shortDistance) {
                            myBeacon = receivedBeacons.get(i);
                            shortDistance = myBeacon.getRssi();
                            Log.d("tagbeacon", String.valueOf(myBeacon.getRssi()));
                        }
                    }
                    Log.d("tagbeacon", String.valueOf(myBeacon.getRssi()));

                    if (myBeacon.getRssi() >= -72) {

                        if (!semaphore) {
                            manager.stopRanging(region);
                            Log.d("tagbeacon", "Entrou no if");
                            beaconId = myBeacon.getUniqueKey();

                            semaphore = true;

                            switch (myBeacon.getUniqueKey()) {

                                case ID_BLUEBERRY:
                                    revealLayout(layoutBlueberry);
                                    layoutActivated = 0;
                                    break;

                                case ID_ICY:
                                    revealLayout(layoutIcy);
                                    layoutActivated = 1;
                                    break;

                                case ID_MINT:
                                    revealLayout(layoutMint);
                                    layoutActivated = 2;
                                    break;

                            }

                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    manager.connect(new BeaconManager.ServiceReadyCallback() {
                                        @Override
                                        public void onServiceReady() {
                                            switch (layoutActivated) {
                                                case 0:
                                                    hideLayout(layoutBlueberry);
                                                    break;

                                                case 1:
                                                    hideLayout(layoutIcy);
                                                    break;

                                                case 2:
                                                    hideLayout(layoutMint);
                                                    break;
                                            }
                                            semaphore = false;
                                            manager.startRanging(region);
                                        }
                                    });
                                }
                            }, 1250);

                        }

                    } else {
//                        if(counter >= 3){
//                            semaphore = false;
//
//                            switch (layoutActivated){
//                                case 0:
//                                    hideLayout(layoutBlueberry);
//                                    break;
//
//                                case 1:
//                                    hideLayout(layoutIcy);
//                                    break;
//
//                                case 2:
//                                    hideLayout(layoutMint);
//                                    break;
//                            }
//                        } else {
//                            counter++;
//                        }

                    }
                }
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void revealLayout(LinearLayout view) {
        int height = view.getHeight() / 2;
        int width = view.getWidth() / 2;
        float radius = (float) Math.hypot(width, height);

        Animator anim = ViewAnimationUtils.createCircularReveal(view, width, height, 0, radius);
        view.setVisibility(View.VISIBLE);
        anim.start();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void hideLayout(final LinearLayout view) {
        int height = view.getHeight() / 2;
        int width = view.getWidth() / 2;
        float radius = (float) Math.hypot(width, height);

        Animator anim = ViewAnimationUtils.createCircularReveal(view, width, height, radius, 0);
        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                view.setVisibility(View.GONE);
            }
        });

        anim.start();
    }

    @Override
    protected void onResume() {
        super.onResume();

        SystemRequirementsChecker.checkWithDefaultDialogs(this);

        manager.connect(new BeaconManager.ServiceReadyCallback() {
            @Override
            public void onServiceReady() {
                manager.startRanging(region);
            }
        });
        Log.d("tagbeacon", "ativou serviço");
    }

    @Override
    protected void onPause() {
        super.onPause();

        manager.stopRanging(region);
    }
}
