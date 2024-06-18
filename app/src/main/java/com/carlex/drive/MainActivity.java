package com.carlex.drive;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import org.json.JSONArray;
import org.json.JSONObject;

import android.os.Process;
import android.*;
import android.app.*;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import java.io.*;
import java.util.*;
import android.content.pm.*;
import android.location.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import androidx.core.app.*;
import android.graphics.*;
import androidx.core.content.*;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;
import java.util.List;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import androidx.annotation.Nullable;
import com.google.android.gms. maps.model.LatLng;
import android.util.Log;
import android.os.Handler;
import android.widget.Toast;
import java.util.Random;
import android.util.DisplayMetrics;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.CameraUpdateFactory;
import android.content.res.Resources;
import android.provider.Settings;
import android.location.LocationListener;                    
import java.io.Serializable;
import android.location.LocationManager;
import android.util.DisplayMetrics;
import android.widget.SeekBar;
import android.content.Intent;
import android.net.Uri;
import android.location.Location;
import java.util.Collections;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.CameraUpdateFactory;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.widget.Toast;
import com.google.android.gms.maps.model.LatLng;
import java.util.List;
import android.app.*;
import android.content.*;
import android.net.*;
import java.text.SimpleDateFormat;
import android.os.*;
import android.view.Gravity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.Intent;
import android.content.ContentUris;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import androidx.appcompat.app.AppCompatActivity;
import uk.me.g4dpz.satellite.GroundStationPosition;
	import uk.me.g4dpz.satellite.SatPos;
	import uk.me.g4dpz.satellite.Satellite;
	import uk.me.g4dpz.satellite.SatelliteFactory;
	import uk.me.g4dpz.satellite.TLE;
import android.Manifest;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.content.SharedPreferences;


import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.widget.ScrollView;

import androidx.annotation.NonNull;

import java.io.DataOutputStream;
import java.io.IOException;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;

//import com.topjohnwu.libsuexample.databinding.ActivityMainBinding;
//import com.topjohnwu.superuser.CallbackList;
import com.topjohnwu.superuser.Shell;
import com.topjohnwu.superuser.ipc.RootService;
import com.topjohnwu.superuser.nio.FileSystemManager;
import android.content.ComponentName;                             import android.content.ServiceConnection;                         import android.os.IBinder;


public class MainActivity extends AppCompatActivity implements OnMapReadyCallback{
  //private SpaceMan spaceMan;
public static boolean processado = false;
private static final int REQUEST_CODE_POST_NOTIFICATIONS = 1;
public static List<LatLng> wPoints  = new ArrayList<>();    
public static List<Float> wBearing  = new ArrayList<>();
public static TextView tbear;
public static TextView tspeed;
public static TextView talti;
public static String sspeed = "";
public static String salti = "";
public static String sbear = "";
public static float currentBearing = 0;
public static double currentAlt = 0;
public static float currentSpeed = 0;
public static double dis =0;
public static double dlat = 0;
public static double dlon = 0;
public static double olat = 0;
public static double olon = 0; 
public static LatLngBounds bounds;
public static MapView mapView;
public static GoogleMap googleMap;
public static long intervaloMillisegundos;
public static Switch checkloc;
public static Switch checkfake;
public static final int permissionRequestCode = 1;
public static boolean mapaCentralizar = false;
public static boolean inicar  = false;
public static boolean iniciarFake  = false;
//pu6blic static TaskExecutor taskExecutor;
public static Marker carMarker;
public static Marker xMarker;
public static Marker oriMarker;
public static Marker desMarker;
public static BitmapDescriptor xIcon;
public static BitmapDescriptor oriIcon;
public static BitmapDescriptor desIcon;
public static BitmapDescriptor carIcon;
public static List<Object[]> rotaFake = new ArrayList<>();
public static Button gerarRotaButton;
public static Button puloButton;
public static Runnable centralizeRunnable;
public static SeekBar turboSeekBar;
public static BitmapDescriptor transparentIcon;
public static List<Long> timeValues = new ArrayList<>();
public static  Handler centralizeHandler = new Handler();
private Handler cellInfoHandler = new Handler();
	private Handler gnssHandler = new Handler();
//private xCellLoc cellLoc;
public static long tempoDecorrido = 0;
public static TextView textViewTempo;
public static float zoom = (float) 15;
public static TextView textViewveloMed;
public static LatLng latLngFakewaze;
public static  Polyline polyline;
public static int desMarkerWidth = 150;
public static int desMarkerHeight = 150;
public static int turbo = 2;
public static int oriMarkerWidth = 160;
public static int oriMarkerHeight = 160;
public static int carMarkerWidth = 135;
public static int carMarkerHeight = 135;
public static double  odometro = 0;
public static CameraPosition cameraPosition;
public static double ttt = 100;
public static LatLng latLng = new LatLng(-23.5879554, -46.63816059);
public static LatLng latLngfake;
public static LocationListener locationListener;
public static xLocationManager locationManager;
public static Toast toast;
public static float mockspeed = (float) 0;                   
public static float mockbearing = (float) 45;
public static double mockalt = 0.0;
public static Context mainApp;
public static Context context;
public static boolean serviceFakeRun = false;
public static ProgressDialog progressDialog;
public static ProgressDialog progressDialog1;
public static ProgressDialog progressDialog2;
public static Bundle savedInstanceState1;
public static boolean wClicked = false;
public static Uri dWaze;
public static ImageView fundo;
private static final String PREF_NAME = "MyPrefs";
private static final String KEY_TURBO = "turbo";
public SharedPreferences prefs;

public static Intent fIntent;
public static String msgLo = "Carregando...";
public static boolean load = false;
public TextView tSat;
public TextView tCel;

public static boolean su = false;
private static final long MINUTE_IN_MILLIS = 15000;



/////
//
//


    







protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	savedInstanceState1 = savedInstanceState;

        setContentView(R.layout.main);




	//textview main
	tbear = findViewById(R.id.tbear);
	tspeed = findViewById(R.id.tspeed);
	talti = findViewById(R.id.talti);
	textViewTempo = findViewById(R.id.tTempo);   
	gerarRotaButton = findViewById(R.id.brota);
	puloButton = findViewById(R.id.bpulo);
	checkloc = findViewById(R.id.checkloc);
	checkfake = findViewById(R.id.checkfake);
	turboSeekBar = findViewById(R.id.turboSeekBar);
	textViewTempo = findViewById(R.id.tTempo);             
	mapView = findViewById(R.id.mapView);
	wClicked = false;
	tSat = findViewById(R.id.tSat);
	tCel = findViewById(R.id.tCel);

	Runnable centralizeRunnable;

	ImageView fundo  = findViewById(R.id.fundo);
	fundo.setVisibility(View.VISIBLE);
	
	//Context static 
	mainApp = getApplicationContext();
	this.context = getApplicationContext();

	getSupportActionBar().hide();

	// Carregar o valor salvo de turbo
        prefs = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        turbo = prefs.getInt(KEY_TURBO, 0);

        // Exemplo: se turbo for 0, definir um valor padrão
        if (turbo == 0) {	
            turbo = 5; // Exemplo de valor padrão
            saveTurboValue(turbo); // Salvar o valor padrão
        }


	// Solicitar permissão POST_NOTIFICATIONS se necessário       
	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
		    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQUEST_CODE_POST_NOTIFICATIONS);         
	    }
	}

	// Verifica permissão salvar log da rota    
	if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) { ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1); } else { }


	// iniciar dados  de localização
	locationManager = xLocationManager.getInstance(mainApp);

	fIntent = new Intent(this, FakeLocationService1.class);	

	////////////Botoes 
	
	//botao getar rota
	gerarRotaButton.setOnClickListener(new View.OnClickListener() {
	   @Override
	   public void onClick(View v) {
		   //if (inicar){
		   onGerarotaClick();
	   //	}
	   }
	});

	//botao salto localizacao teletransporte
	puloButton.setOnClickListener(new View.OnClickListener() {
		@Override                                        
   		public void onClick(View v) {
		//	if (inicar)
                	onSaltoClick();
		}      
	});

	////////////controladores 
	
	//controlador ligar desligar fake
	checkfake.setOnCheckedChangeListener
		(new CompoundButton.OnCheckedChangeListener() {

		@Override
		public void onCheckedChanged
		(CompoundButton buttonView, boolean isChecked) {
		    //if (MainActivity.inicar){
			//definir estado fake
			iniciarFake = isChecked;
			checkfake.setClickable(iniciarFake);      
			checkfake.setEnabled(iniciarFake);     

			if (MainActivity.odometro>0){
				turboSeekBar.setClickable(iniciarFake);     
				turboSeekBar.setEnabled(iniciarFake);
			}

			//desligar limpar dados
			if (!isChecked){
				rotaFake = new ArrayList<>();
				if (polyline != null) {
    						polyline.remove();
    						polyline = null; 
				}
				stopFakeLoc();
				//mapaCentralizar = true;
			} else {
			}
		    //}
		}
	});


	//controle centralizar mapa localizacao
	checkloc.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

		    //definir estado map
		    //if (inicar){
			mapaCentralizar = isChecked;
			checkloc.setClickable(!mapaCentralizar);  
			checkloc.setChecked(
					mapaCentralizar);
			xMarker.setVisible(!mapaCentralizar);
			gerarRotaButton.setEnabled(!mapaCentralizar);       
			gerarRotaButton.setClickable(!mapaCentralizar);
			puloButton.setEnabled(!mapaCentralizar); 
			puloButton.setClickable(!mapaCentralizar);
		
		    //}
		}
	});

	///////////////controle deslizante 
	
	//controle turbo velocidade deslocamento rota 
	turboSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

		@Override
		public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

			//definir valor turbo
			//if (MainActivity.inicar){
				turbo = progress;
			//}
		
        	}	

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {}

			
		@Override                                           
		public void onStopTrackingTouch(SeekBar seekBar) {}	

	});

	//mapView.setVisibility(View.VISIBLE);

}


@Override
protected void onPostCreate(Bundle savedInstanceState) {
    super.onPostCreate(savedInstanceState);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(this);
}





protected void onStart() {
        super.onStart();
	new VerifyLocationTask(this, locationManager).execute();
	if (!FakeLocationService1.isServiceRunning()) {     
		//checkloc.setChecked(true);       
		iniciarFake=true;          
		checkfake.setChecked(true);
	}
}
	

private boolean isDeviceRooted() {
    // Verifica se o arquivo su está presente
    File file = new File("/system/xbin/su");
    if (file.exists()) {
        return true;
    }

    // Verifica se o arquivo su está presente
    file = new File("/system/bin/su");
    return file.exists();
}



@Override
protected void onResume() {         
    super.onResume();                

    boolean isRooted = isDeviceRooted();
    mapView.onResume();            
    mapView.setVisibility(View.VISIBLE);

    inicar=true;
    if (!FakeLocationService1.isServiceRunning()) {                 
        //checkloc.setChecked(true);         
	checkfake.setChecked(true);
	iniciarFake=true;                                   
    }
    stopOverlayService();                        
    Intent intent = getIntent();
    if (intent != null && intent.getData() != null && "waze".equals(intent.getData().getScheme())) {
       if (dWaze != intent.getData()){
	       wClicked = false;
       }
       if (!wClicked){
	       wClicked = true;                   
	       dWaze = intent.getData();          
	       String query = dWaze.getQuery();
	       if (query != null) {
		       handleIntent(query);                
	       }
       }
    }
}


private String readRawTextFile(int resId) {
        InputStream inputStream = getResources().openRawResource(resId);
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        StringBuilder sb = new StringBuilder();
        String line;
        try {
            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line).append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }

public static  boolean isServiceRunning(Class<?> serviceClass) {
    ActivityManager manager = (ActivityManager) MainActivity.mainApp.getSystemService(Context.ACTIVITY_SERVICE);
    for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
        if (serviceClass.getName().equals(service.service.getClassName())) {
            return true;
        }
    }
    return false;
}


private void saveTurboValue(int value) {
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(KEY_TURBO, value);
        editor.apply();
    }




public void startFakeLoc() {
    if (!FakeLocationService1.isServiceRunning()) {
        ContextCompat.startForegroundService(this, fIntent);
    }
}

public void stopFakeLoc() {
    if (FakeLocationService1.isServiceRunning()) {
        stopService(fIntent);
    }
}



public void mToast(String msg) {
	toast = Toast.makeText(this, msg, Toast.LENGTH_SHORT);   
	toast.show();
}


public void onGerarotaClick() {
        // Check dados gerar rota
        if (latLng == null || latLngfake == null) {
            mToast("Erro dados rota");
            return;
        }

        // Check distancia
        double dist = calcularDistancia(latLng, latLngfake);
        if (dist < 10) {
            mToast("Rota muito curta " + dist);
            return;
        }
        if (dist > 50000) {
            mToast("Rota muito Longa " + dist);
            return;
        }



        // Iniciar nova 
        odometro = 0;
        intervaloMillisegundos = 0;
        rotaFake = new ArrayList<>();
        Object[] dadosSegmento = new Object[]{0, latLng, currentBearing, 0.0, 100.0, 0.0, 100L, 0.0, 0.0};
        rotaFake.add(dadosSegmento);
	salvarRotafakeEmArquivo();


	if (!FakeLocationService1.isServiceRunning()) { 
		startFakeLoc(); 
	}

        //mapaCentralizar = true;
        iniciarFake = true;
        checkfake.setChecked(true);
        ///checkloc.setChecked(true);

        // Limpar rota antiga
        if (polyline != null) {
            polyline.remove();
            polyline = null;
        }

        // Solicitar dados API Google Maps
        getrota(latLng, latLngfake);
        mToast("Gerando rota para: \n" + latLngfake.toString());


//	progressDialog = new ProgressDialog(this);
//	msgLo = "Carregando...";
//	load = true;
//	progressDialog.setMessage(msgLo);                             progressDialog.setCancelable(false);                          progressDialog.show();
}


public void onSaltoClick(){
        //check dados mapa
        if (googleMap  == null) { 
		mToast("Mapa nao carregado");
		return;
	}

	//obter nova cordenada salto             
	latLngfake = cameraPosition.target;      

	//limpar dados fake e salvar novo ponto      
	rotaFake = new ArrayList<>();         
	Object[] dadosSegmento = new Object[]{0, latLngfake, currentBearing, 0.0, 100.0, 0.0, (long) 100, 0.0, 0.0};
	rotaFake.add(dadosSegmento);
	salvarRotafakeEmArquivo();



	if (!FakeLocationService1.isServiceRunning()) {      
		startFakeLoc();            
	}

	//iniciar fake                       
	//mapaCentralizar = true;             
	iniciarFake = true;                  
	checkfake.setChecked(true);                                        
	//checkloc.setChecked(true);

	//limpar rota antiga          
	if (polyline != null) {           
		polyline.remove();           
		polyline = null;           
	}

	toast = Toast.makeText(MainActivity.this, "Teletransportado para: ", Toast.LENGTH_SHORT);
	toast.show();
}


//permisao sobreposivao overlaya
@Override
protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == 1234) {
        if (Settings.canDrawOverlays(MainActivity.this)) {
            //startOverlayService();
        } else {
            Toast.makeText(MainActivity.this, "Permissão de sobreposição negada", Toast.LENGTH_SHORT).show();
        }
    }
}

//inicar overlay
private void startOverlayService() {
    Intent ointent = new Intent(MainActivity.this, OverlayService.class);
    startService(ointent);
}



//controle chronometro
private String formatarTempo(long millis) {
    long minutos = (millis / 60000);
    long segundos = (millis / 1000) % 60;
    long centesimos = (millis / 10) % 100;
    long milesimos = millis % 1000;
    return String.format("%02d:%02d:%02d", minutos, segundos, centesimos);
}


@Override
protected void onNewIntent(Intent intent) {
	super.onNewIntent(intent);
        setIntent(intent);
}


private void handleIntent(String query) {
        if (query != null) {
            String[] params = query.split("&");
            double latitude = 0;
            double longitude = 0;
            boolean navigate = false;
            for (String param : params) {
                String[] keyValue = param.split("=");
                if (keyValue.length == 2) {
                    if (keyValue[0].equals("ll")) {
                        String[] latLng = keyValue[1].split(",");
                        if (latLng.length == 2) {
                            latitude = Double.parseDouble(latLng[0]);
                            longitude = Double.parseDouble(latLng[1]);
                        }
                    } else if (keyValue[0].equals("navigate")) {
                        navigate = Boolean.parseBoolean(keyValue[1]);
                    }
                }
            }
            // Definir ponto destino rota
            latLngFakewaze = new LatLng(latitude, longitude);
	    latLngfake = latLngFakewaze;                    
	    onGerarotaClick();                      
	    mToast("Ativando Auto Rota Fake Waze");
	}
}



//Solicitar dados api google 
public void getrota(LatLng ORIGEM, LatLng DESTINO){
    List<SegmentoRota> segmentosRota = new ArrayList<>();
    ObterSegmentosRota.obterSegmentosRota(ORIGEM, DESTINO, new ObterSegmentosRota.OnSegmentosRotaListener() {
        public void onSegmentosRota(List<ObterSegmentosRota.Segmento> segmentos) {
            if (segmentos != null) {
                for (ObterSegmentosRota.Segmento segmento : segmentos) {
                    double tdistancia = segmento.gettDistancia();
                    long tempo = (long) segmento.getTempo();
                    List<LatLng> pontos = segmento.getPontos();
                    SegmentoRota segmentoRota = new SegmentoRota(tdistancia, tempo, pontos);
                    segmentosRota.add(segmentoRota);
                }
            }
            processarSegmentosRota(segmentosRota);
        }
    });
}

//dados rota bruta
public class SegmentoRota {
    private double tdistancia;
    private long tempo;                                                                             private List<LatLng> pontos;

    public SegmentoRota(double tdistancia, long tempo, List<LatLng> pontos) {
        this.tdistancia = tdistancia;
        this.tempo = (long) tempo;
        this.pontos = pontos;                                                                       }                                                                                           
    // Métodos para acessar os dados do segmento
    public double gettDistancia() {
        return tdistancia;
    }                                                                                                                                                                 public long getTempo() {
        return tempo;
    }

    public List<LatLng> getPontos() {
        return pontos;                                                                              }                                                            
}





/*


    public void getrota(LatLng origem, LatLng destino) {
        List<SegmentoRota> segmentosRota = new ArrayList<>();
        ObterSegmentosRotaOpen.obterSegmentosRota(origem, destino, new ObterSegmentosRotaOpen.OnSegmentosRotaListener() {
            @Override
            public void onSegmentosRota(List<ObterSegmentosRotaOpen.Segmento> segmentos) {
                if (segmentos != null) {
                    for (ObterSegmentosRotaOpen.Segmento segmento : segmentos) {
                        double tdistancia = segmento.gettDistancia();
                        double tempo = segmento.getTempo();
                        List<LatLng> pontos = segmento.getPontos();
                        SegmentoRota segmentoRota = new SegmentoRota(tdistancia, tempo, pontos);
                        segmentosRota.add(segmentoRota);
                    }
                }
                processarSegmentosRota(segmentosRota);
            }
        });
    
    }

    public static class SegmentoRota {
        private double tdistancia;
        private double tempo;
        private List<LatLng> pontos;

        public SegmentoRota(double tdistancia, double tempo, List<LatLng> pontos) {
            this.tdistancia = tdistancia;
            this.tempo = tempo;
            this.pontos = pontos;
        }

        public double gettDistancia() {
            return tdistancia;
        }

        public double getTempo() {
            return tempo;
        }

        public List<LatLng> getPontos() {
            return pontos;
        }
    }

*/


private static float[] computeDistanceAndBearing(double lat1, double lon1, double lat2, double lon2) {
        int MAXITERS = 20;
        lat1 *= Math.PI / 180.0;
        lat2 *= Math.PI / 180.0;
        lon1 *= Math.PI / 180.0;
        lon2 *= Math.PI / 180.0;

        double a = 6378137.0;
        double b = 6356752.3142;
        double f = (a - b) / a;
        double aSqMinusBSqOverBSq = (a * a - b * b) / (b * b);

        double L = lon2 - lon1;
        double A = 0.0;
        double U1 = Math.atan((1.0 - f) * Math.tan(lat1));
        double U2 = Math.atan((1.0 - f) * Math.tan(lat2));

        double cosU1 = Math.cos(U1);
        double cosU2 = Math.cos(U2);
        double sinU1 = Math.sin(U1);
        double sinU2 = Math.sin(U2);
        double cosU1cosU2 = cosU1 * cosU2;
        double sinU1sinU2 = sinU1 * sinU2;

        double sigma = 0.0;
        double deltaSigma = 0.0;
        double cosSqAlpha = 0.0;
        double cos2SM = 0.0;
        double cosSigma = 0.0;
        double sinSigma = 0.0;
        double cosLambda = 0.0;
        double sinLambda = 0.0;

        double lambda = L;
        for (int iter = 0; iter < MAXITERS; iter++) {
            double lambdaOrig = lambda;
            cosLambda = Math.cos(lambda);
            sinLambda = Math.sin(lambda);
            double t1 = cosU2 * sinLambda;
            double t2 = cosU1 * sinU2 - sinU1 * cosU2 * cosLambda;
            double sinSqSigma = t1 * t1 + t2 * t2;
            sinSigma = Math.sqrt(sinSqSigma);
            cosSigma = sinU1sinU2 + cosU1cosU2 * cosLambda;
            sigma = Math.atan2(sinSigma, cosSigma);
            double sinAlpha = (sinSigma == 0) ? 0.0 :
				cosU1cosU2 * sinLambda / sinSigma;
            cosSqAlpha = 1.0 - sinAlpha * sinAlpha;
            cos2SM = (cosSqAlpha == 0) ? 0.0 :
				cosSigma - 2.0 * sinU1sinU2 / cosSqAlpha;

            double uSquared = cosSqAlpha * aSqMinusBSqOverBSq;
            A = 1 + (uSquared / 16384.0) *
				(4096.0 + uSquared *
				(-768 + uSquared * (320.0 - 175.0 * uSquared)));
            double B = (uSquared / 1024.0) *
				(256.0 + uSquared *
				(-128.0 + uSquared * (74.0 - 47.0 * uSquared)));
            double C = (f / 16.0) *
				cosSqAlpha *
				(4.0 + f * (4.0 - 3.0 * cosSqAlpha));
            double cos2SMSq = cos2SM * cos2SM;
            deltaSigma = B * sinSigma *
				(cos2SM + (B / 4.0) *
				(cosSigma * (-1.0 + 2.0 * cos2SMSq) -
				(B / 6.0) * cos2SM *
				(-3.0 + 4.0 * sinSigma * sinSigma) *
				(-3.0 + 4.0 * cos2SMSq)));

            lambda = L +
				(1.0 - C) * f * sinAlpha *
				(sigma + C * sinSigma *
				(cos2SM + C * cosSigma *
				(-1.0 + 2.0 * cos2SM * cos2SM)));

            double delta = (lambda - lambdaOrig) / lambda;
            if (Math.abs(delta) < 1.0e-12) {
                break;
            }
        }

        float distance = (float) (b * A * (sigma - deltaSigma));
        float initialBearing = (float) Math.atan2(cosU2 * sinLambda,
												  cosU1 * sinU2 - sinU1 * cosU2 * cosLambda);
        initialBearing *= 180.0 / Math.PI;
        float finalBearing = (float) Math.atan2(cosU1 * sinLambda,
												-sinU1 * cosU2 + cosU1 * sinU2 * cosLambda);
        finalBearing *= 180.0 / Math.PI;

        return new float[]{distance, initialBearing, finalBearing};
    }


//processar resposta dados api
public void processarSegmentosRota(List<SegmentoRota> segmentosRota) {
    rotaFake = new ArrayList<>();
    List<LatLng> poliFake = new ArrayList<>();
    for (int indiceSegmento = 0; indiceSegmento < segmentosRota.size(); indiceSegmento++) {
        SegmentoRota segmentoRota = segmentosRota.get(indiceSegmento);
        List<LatLng> pontos = segmentoRota.getPontos();	

	long tempooEmSegundos = (long) segmentoRota.getTempo()	;
	double distanciatotalmetros = segmentoRota.gettDistancia();
	double velocidade = (distanciatotalmetros / tempooEmSegundos);

	double freio = 0;

	//gerar maior resolucao 0.5cm
	maioresolucao m1 = new maioresolucao();
        pontos = m1.maioresolucao(pontos);


	//procesamento inicial  lista pontos rotafake
        for (int i = 1; i < pontos.size() - 1; i++) {
            LatLng pontoAtual = pontos.get(i-1);
            LatLng proximoPonto = pontos.get(i);

	    float[] resultado = computeDistanceAndBearing(pontoAtual.latitude, pontoAtual.longitude, proximoPonto.latitude, proximoPonto.longitude);


	    float bearing =  resultado[1];
            float distancia =  resultado[0];

	    //bearing = (float) ((bearing + 180.0) % 360.0 - 180.0);


            //float bearing = calcularBearing(pontoAtual, proximoPonto);
	    //double distancia = calcularDistancia(pontoAtual, proximoPonto);

	    double tempoo = (distancia / velocidade)*1000;
            Object[] dadosSegmento = new Object[]{
		    indiceSegmento, 
		    //pontoAtual, 
		    proximoPonto,
		    bearing, velocidade, tempoo, distancia, tempooEmSegundos, distanciatotalmetros, freio};
            rotaFake.add(dadosSegmento);

	    //adicionar pontos nova poliline
	    poliFake.add(pontoAtual);
        }
    }

    //suavizar pontos curvas remover ponto intermediario
    maioresolucao m = new maioresolucao();            
    poliFake = m.suavizarCoordenadas(poliFake);

    //gerar poliline
    drawRoute(poliFake);

    //trocar pontos poliline  em rotafake 
    for (int i = 0; i < rotaFake.size(); i++) {
    	Object[] dadosSegmento = rotaFake.get(i);
    	LatLng pontoAtual = (LatLng) dadosSegmento[1]; 
    	if (i < poliFake.size()) {
        	LatLng pontoFake = poliFake.get(i); 
        	dadosSegmento[1] = pontoFake; 
    	}
     }

     // Recalcular os valores de distância e bering 
     for (int i = 1; i < rotaFake.size()-1; i++) {
	Object[] proximoDadosSegmento = rotaFake.get(i);
    	Object[] dadosSegmento = rotaFake.get(i-1);
    	LatLng pontoAtual = (LatLng) dadosSegmento[1]; 
    	LatLng proximoPonto = null;
    	//if (i < rotaFake.size()) {
        	//proximoDadosSegmento = rotaFake.get(i);
        	proximoPonto = (LatLng) proximoDadosSegmento[1]; 
    	//}
    	//if (proximoPonto != null) {

		float[] resultado = computeDistanceAndBearing(pontoAtual.latitude, pontoAtual.longitude, proximoPonto.latitude, proximoPonto.longitude);                                                                                     
		float bearing =  resultado[1];
		float distancia =  resultado[0];                                                      
		//bearing = (float) ((bearing + 180.0) % 360.0 - 180.0);
        	//float bearing = calcularBearing(pontoAtual, proximoPonto);
        	//double distancia = calcularDistancia(pontoAtual, proximoPonto); 
		proximoDadosSegmento[2] = bearing;
		proximoDadosSegmento[5] = distancia;
        	//dadosSegmento[2] = bearing; 
        	//dadosSegmento[5] = distancia;
    	//}
	rotaFake.set(i, proximoDadosSegmento);
    	//rotaFake.set(i, dadosSegmento); 
     }

     //simular freio  curvas
     simularMovimento();

     //simular acelerador curvas
     Collections.reverse(rotaFake);
     simularMovimento();

     //recalcular tempo e velocidade pontos rota 
     Collections.reverse(rotaFake);
     procesarrMovimento();

     salvarRotafakeEmArquivo();
}


/*
private void salvarRotafakeEmArquivo() {
    new AsyncTask<Void, Void, Void>() {
        @Override
        protected Void doInBackground(Void... voids) {
            try {
                File file = new File(getExternalFilesDir(null), "rota_fake.log");
                FileOutputStream fileOutputStream = new FileOutputStream(file);
                double toseg = 0;
                long ttseg = 0;
		long cTime = System.currentTimeMillis() + 5000;

		MyApp.getDatabase().rotaFakeDao().deleteAllExceptFirstFour();
		

                for (Object[] dadosSegmento : rotaFake) {
                    int indiceSegmento = (int) dadosSegmento[0];
                    LatLng pontoAtual = (LatLng) dadosSegmento[1];
                    float bearing = (float) dadosSegmento[2];
                    double velocidade = (double) dadosSegmento[3];
                    double tempoo = (double) dadosSegmento[4];
		    long tempo1 = (long) tempoo;

		    cTime += tempo1;

                    String formattedTime = formatarTempo(cTime);


                    String linha = "Segmento: " + indiceSegmento +
                            ", Cordenada " + pontoAtual.toString() +
                            ", Bearing: " + String.format("%.2f",bearing) +
                            ", Velocidade: " + String.format("%.2f",velocidade) +
                            ", Tempo: " + formattedTime  + "\n";

                    fileOutputStream.write(linha.get0Bytes());

                    RotaFake rotaFakeEntry = new RotaFake(pontoAtual.latitude, pontoAtual.longitude, bearing, velocidade, (long) cTime);
                    MyApp.getDatabase().rotaFakeDao().insert(rotaFakeEntry);
                }
                fileOutputStream.close();
                //toast = Toast.makeText(MainActivity.this, "Dados salvos em: " + file.getAbsolutePath(), Toast.LENGTH_LONG);
                //toast.show();

                startFakeLoc();
		hideProgressDialog();

            } catch (IOException e) {
                e.printStackTrace();
                //Toast.makeText(MainActivity.this, "Erro ao salvar os dados: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
            return null;
        }
    }.execute();
}*/


private void salvarRotafakeEmArquivo() {
    new AsyncTask<Void, Void, String>() {
        @Override
        protected String doInBackground(Void... voids) {
            try {
                long cTime = System.currentTimeMillis() + 5000;

                // Remover todos os registros exceto

                MyApp.getDatabase().rotaFakeDao().deleteAllExceptFirstFour();

		//Intent intent = new Intent("com.example.ACTION_STOP_SERVICE");
		//sendBroadcast(intent);


                for (Object[] dadosSegmento : rotaFake) {
                    int indiceSegmento = (int) dadosSegmento[0];
                    LatLng pontoAtual = (LatLng) dadosSegmento[1];
                    float bearing = (float) dadosSegmento[2];
                    double velocidade = (double) dadosSegmento[3]*(turbo+1);
                    double tempoo = (double) dadosSegmento[4];
                    long tempo1 = (long) tempoo/(turbo+1);

                    cTime += tempo1;

                    RotaFake rotaFakeEntry = new RotaFake(pontoAtual.latitude, pontoAtual.longitude, bearing, velocidade, cTime);
                    MyApp.getDatabase().rotaFakeDao().insert(rotaFakeEntry);
                }

                return "Dados salvos no banco de dados com sucesso. turbo:" + turbo;

            } catch (Exception e) {
                e.printStackTrace();
       		         return "Erro ao salvar os dados: " + e.getMessage();
            }
        }

        @Override
        protected void onPostExecute(String result) {
	    //startFakeLoc();
	    //hideProgressDialog();
        }
    }.execute();
}



//pre processo final rota fake
private void procesarrMovimento() {
    if (rotaFake == null || rotaFake.isEmpty()) {
        Log.e("simularMovimento", "A lista rotaFake está vazia ou não inicializada.");
        return;
    }

    int indiceSegmento = 0;
    double tempototal = 100; // tempo total fixo para distribuição
    int numerosegmentos = 0;
    double sumntempo = 0.0;
    double nntempo =0;
    double tssum = 0;
    double ttotal =0;
    int ii = 0; // índice inicial para o loop
    int i=0;
    // Iterar sobre a lista rotaFake
    for (i = ii; i < rotaFake.size(); i++) {
        // Contar número de segmentos com o mesmo índice
        Object[] dadosSegmento = rotaFake.get(i);
        double ntempo = (double) dadosSegmento[4];
        numerosegmentos++;

        // Atualizar o tempo total
        sumntempo += ntempo;

        // Verificar início de um novo índice de segmento
        if (indiceSegmento < (int) dadosSegmento[0]) {
            // Iterar novamente sobre rotaFake de ii até i-1
            for (int j = ii; j < i; j++) {
                Object[] NdadosSegmento = rotaFake.get(j);
                ntempo = (double) NdadosSegmento[4];

                // Distribuir ponderadamente o valor de tempototal
                double peso = (ntempo / sumntempo) * numerosegmentos;
                ntempo = (tempototal / numerosegmentos) * peso;

                NdadosSegmento[4] = ntempo;
                rotaFake.set(j, NdadosSegmento);
            }
            // Resetar variáveis para o próximo segmento
            numerosegmentos = 0;
            sumntempo = 0.0;
            ii = i;
        }


	if (indiceSegmento != (int) dadosSegmento[0]){
		ttotal += tempototal;
	}
        // Atualizar o índice do segmento
	long tempotlong = (long) dadosSegmento[6];
	tempototal = (double) (tempotlong*1000);
        indiceSegmento = (int) dadosSegmento[0];
    }	


    double tt = 0;

    for (int j = 0; j < rotaFake.size(); j++) {   
	 Object[] NdadosSegmento = rotaFake.get(j);
	 double ntempo = (double) NdadosSegmento[4];
	 tt += ntempo;
    }


    String a = "t"+ formatarTempo(((long) tt/1000));
    mToast(a);

 a = "tt"+ formatarTempo(((long) ttotal /1000));    
 mToast(a);

    for (int j = 0; j < rotaFake.size(); j++) {
       Object[] NdadosSegmento = rotaFake.get(j);    
	double ntempo = (double) NdadosSegmento[4];              
	// Distribuir ponderadamente o valor de ttotal
        double peso = (ntempo / tt) * numerosegmentos;
	ntempo = (ttotal / numerosegmentos) * peso;             
	NdadosSegmento[4] = ntempo;
	rotaFake.set(j, NdadosSegmento);
    }



    procesarrMovimentofim();
}


//proceso etapa final geracao  rotafake
private void procesarrMovimentofim() {   
	if (rotaFake == null || rotaFake.isEmpty()) {     
		Log.e("simularMovimento", "A lista rotaFake está vazia ou não inicializada.");   
		return;             
	}                                                    
	// Recalcular os valores de tempo e velocidade 
	for (int i = 0; i < (rotaFake.size()-1); i++) {   
		Object[] dadosSegmento = rotaFake.get(i);    
		double velocidade = (double) dadosSegmento[3];
		double freio  = (double) dadosSegmento[8]; 
		//double ntempo = (double) dadosSegmento[4]; 
		velocidade *= (1 - freio);
		velocidade = Math.max(2, Math.min(velocidade, 60));
		float distancia = (float) dadosSegmento[5]; 
		double ntempo = (distancia / velocidade) * 1000;
		dadosSegmento[3] = (double) velocidade;
		dadosSegmento[4] = (double) ntempo;    
		rotaFake.set(i, dadosSegmento); 
	}
}




//processo simular freio aceleracao curvasi
private void simularMovimento() {
    if (rotaFake == null || rotaFake.isEmpty()) {
        Log.e("simularMovimento", "A lista rotaFake está vazia ou não inicializada.");
        return;
    }

    double nfreio = 0.95;
    int x;
    int j;
    int i;
    int jj;

    
    // freio inial/final
    for (i = 0; i < Math.min(20, rotaFake.size()); i++) {
        Object[] dadosSegmento = rotaFake.get(i);
        dadosSegmento[8] = nfreio;
        rotaFake.set(i, dadosSegmento);
	nfreio = Math.max(0.0, nfreio - 0.01); 
    }

    //suavizar mudanca  velocudade freio/acelerador
    int ss = rotaFake.size()-20;
    for (i = 20; i < ss - 1; i++) {
    Object[] sdadosSegmento = rotaFake.get(i);
      double speedAtual = (double) sdadosSegmento[3];
      Object[] sdadosProxSegmento = rotaFake.get(i + 1);
      double proxspeed = (double) sdadosProxSegmento[3];
      double diffSpeed = Math.abs(proxspeed - speedAtual);
      boolean p1 = false; 
      if (proxspeed > speedAtual){
	p1 = true;
      }


      if (diffSpeed > 0) {
	double ps = diffSpeed / 18;
	int xx = i;
	while (diffSpeed>0){
		if (xx >= rotaFake.size()) {
			diffSpeed = 0;
                	break; 
		}
		if (xx <= 36) {             
			diffSpeed = 0;
			break;
                }
		Object[] AdadosSegmento =  rotaFake.get(xx);
		double aspeedAtual = (double) AdadosSegmento[3];
		if (p1){
			AdadosSegmento[3] = aspeedAtual + diffSpeed;
		}else{
			AdadosSegmento[3] = aspeedAtual - diffSpeed;
		}

		rotaFake.set(xx, AdadosSegmento); 
		diffSpeed -= ps ;
		xx--;
        }
      }
    }

    //simulafor freio/acelerador curva
    for (i = 0; i < (ss-18); i++) {
        //float diffBearing = 0;
	//double diffSpeed = 0;
	//double parteSeed = 0;
	int turboo = 7;
        
	Object[] dadosSegmento = rotaFake.get(i);
        float bearingAtual = (float) dadosSegmento[2];
	double velocidadeAtual = (double) dadosSegmento[3];
	float distcu = (float) dadosSegmento[5];


	Object[] dadosProximoSegmento = rotaFake.get(i+9);
        float bearingProximo = (float) dadosProximoSegmento[2];
	int diffBearing = dBearing(bearingAtual, bearingProximo);

	if(diffBearing>90)
		diffBearing=90;
        
	j=i+9;
		
      	if (diffBearing>10){
	   for (jj = 10*turboo; jj > 0; jj--) {
		if (j <= 0) { break; }
		dadosSegmento = rotaFake.get(j);
		double fatorReducao = fator(diffBearing,jj/turboo);
		velocidadeAtual = (double) dadosSegmento[3];
		dadosSegmento[0] = (int) diffBearing;
		//velocidadeAtual *= fatorReducao;
		dadosSegmento[3] = (double) velocidadeAtual;
		double fre = fatorReducao + (double) dadosSegmento[8];
		fre = Math.max(0, Math.min(fre, 0.90));
		dadosSegmento[8] = (double) fre;
		rotaFake.set(j, dadosSegmento);
		j--;
	   }
	   i=i+9;
	}
    }

    Log.i("simularMovimento", "Simulação concluída.");
}


//calcular fator acereador/freio
public Double fator(int angulo, int tempo) {
        double x = angulo;
        double y = tempo;
        
        double P = 0.01125 * x - 0.1125;
        double Sp = 0.09 * y + 0.1;
        
        double z = P * Sp;
        
        return z;
    }

//calcular diferenca angulo pontos
public int dBearing(double bearing1, double bearing2){
        double diff1 = Math.abs(bearing1 - bearing2);
        double diff2 = 360 - Math.abs(bearing1 - bearing2);
        double diffBearing = Math.min(diff1, diff2);
        return (int) diffBearing;
}
	

//calcular distancia pontos
private double calcularDistancia(LatLng pontoA, LatLng pontoB) {
    double earthRadius = 6371;   
    double lat1 = Math.toRadians(pontoA.latitude);
    double lon1 = Math.toRadians(pontoA.longitude);               
    double lat2 = Math.toRadians(pontoB.latitude);      
    double lon2 = Math.toRadians(pontoB.longitude);    
    double dLat = lat2 - lat1;                         
    double dLon = lon2 - lon1;                     
    double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +      
	    Math.cos(lat1) * Math.cos(lat2) *           
	    Math.sin(dLon / 2) * Math.sin(dLon / 2);       
    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    double distance = earthRadius * c * 1000; 
    return distance;                                             
}

//calcular bearing
private float calcularBearing(LatLng pontoA, LatLng pontoB) {
    double lat1 = Math.toRadians(pontoA.latitude);
    double lon1 = Math.toRadians(pontoA.longitude);
    double lat2 = Math.toRadians(pontoB.latitude);
    double lon2 = Math.toRadians(pontoB.longitude);
    double dLon = lon2 - lon1;
    double y = Math.sin(dLon) * Math.cos(lat2);
    double x = Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1) * Math.cos(lat2) * Math.cos(dLon);
    double bearing = Math.toDegrees(Math.atan2(y, x));
    return (float) ((bearing + 360) % 360);
}


//desenhar poliline
public void drawRoute(List<LatLng> routePoints) {
	if (polyline != null) {
    		polyline.remove();
    		polyline = null; 
	}

	//Exibir marcadores inicio fim rota
	LatLng inicio = routePoints.get(0);
	LatLng fim = routePoints.get(routePoints.size() - 1);
	oriMarker.setPosition(inicio);         
	desMarker.setPosition(fim);

	PolylineOptions polylineOptions = new PolylineOptions();
	polylineOptions.width(5);
	polylineOptions.color(Color.argb(255, 255, 165, 0)); 

	for (LatLng point : routePoints) {
		polylineOptions.add(point);
	}
	polyline = googleMap.addPolyline(polylineOptions);


	LatLng center = latLng;

	if (polyline != null) {                                 
		LatLngBounds.Builder builder = new LatLngBounds.Builder();         
		for (LatLng point : polyline.getPoints()) { builder.include(point); }         
		builder.include(latLng);

                bounds = builder.build();                       
		int padding = 50;           

                zoom = calculateZoomfrom(latLng, polyline);   
		//zoom = (float) 15;         
		center = bounds.getCenter();

	} else { zoom = (float) 15;}

                // mover camera atualizar marcadorez      
		CameraPosition cameraPosition = new CameraPosition.Builder().target(center).zoom(zoom).build();      
		googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));                 
}

/*
private Runnable cellInfoRunnable = new Runnable() {
		public void run() {
			JSONObject jsonResponse;
			double lat1 = 0.0, lon1 = 0.0, alt1 = 0.0;
			if (latLng!=null){
				lat1 = latLng.latitude;
				lon1 = latLng.longitude;
				alt1 = currentAlt;
			}
			jsonResponse =  Cellinfo.Cellinfo(lat1, lon1);
			if (jsonResponse!=null){
				tCel.setText(jsonResponse.toString());
			}
			cellInfoHandler.postDelayed(this, 10000); 
		}
	};
	
	private Runnable gnssRunnable = new Runnable() {
		public void run() {
			String tles = readRawTextFile(R.raw.gps);
			double lat1 = 0.0, lon1 = 0.0, alt1 = 0.0;
			if (latLng!=null){
			lat1 = latLng.latitude;           
			lon1 = latLng.longitude;
			alt1 = currentAlt;
			}
			spaceMan = new SpaceMan(tles, lat1, lon1, alt1);
			sting log = "";
			for (SatelliteInfo si: spaceMan) {
				log += "\n[";
				log += ("SAT " + si.tle.getName() + " above_horizon " + si.satPos.isAboveHorizon());
				log += (", azi " + si.satPos.getAzimuth() + " ele " + si.satPos.getElevation() +
					", lon " + si.satPos.getLongitude() + ", lat " + si.satPos.getLatitude() + ", alt " + si.satPos.getAltitude())+ "] ";
			}
			tSat.setText(log);
			gnssHandler.postDelayed(this, 1000); 
		}
	};

*/








//atualizar mapa e controles
public void centralizar(){
	if (googleMap == null) {
		return;
	}

	//cellInfoHandler.postDelayed(cellInfoRunnable, 10000);
	//gnssHandler.postDelayed(gnssRunnable, 1000);


	String schro = "00:00:00";

        new AsyncTask<Void, Void, RotaFake>() {
        @Override
        protected RotaFake doInBackground(Void... voids) {
            AppDatabase db = MyApp.getDatabase();
            if (db != null) {
                return db.rotaFakeDao().getLastRotaFakeByTime();
            }
            return null;
        }

        @Override
        protected void onPostExecute(RotaFake lastRotaFake) {
            if (lastRotaFake != null) {
                long chegada = lastRotaFake.getTempo();
		if (chegada > System.currentTimeMillis()+200) {       
			long chronometrodecresivo = chegada - System.currentTimeMillis();                                      
			long hours = TimeUnit.MILLISECONDS.toHours(chronometrodecresivo);
			long minutes = TimeUnit.MILLISECONDS.toMinutes(chronometrodecresivo) - TimeUnit.HOURS.toMinutes(hours);
			long seconds = TimeUnit.MILLISECONDS.toSeconds(chronometrodecresivo) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(chronometrodecresivo));
			textViewTempo.setText("🏁 " + String.format("%02d:%02d:%02d", hours, minutes, seconds));            
		}
		else 
			textViewTempo.setText("🏁 00:00:00");
            }
        }}.execute();


	if (polyline != null) {      
		desMarker.setVisible(true);  
		oriMarker.setVisible(true);
	} else {
		desMarker.setVisible(false);      
		oriMarker.setVisible(false);
	}


	//atualizar textview
	sspeed = String.format("⏱️ %.1f", currentSpeed*3.6*4)+ " km/h";
	salti = String.format("🏔️%.1f", currentAlt)+ "m";

	tspeed.setText(sspeed);    
	talti.setText(salti);            



	//posicao marcador localizacao
	if (latLng!=null){
	carMarker.setPosition(latLng);                        
	carMarker.setRotation(currentBearing);
	

	//centralizar mapa e camera
	if (!mapaCentralizar){
		cameraPosition = googleMap.getCameraPosition();
		latLngfake = cameraPosition.target;
		dlat  = latLngfake.latitude;
		dlon = latLngfake.longitude;
		xMarker.setPosition(latLngfake);
		xMarker.setVisible(true);
	} else {
		xMarker.setVisible(false);
		//gerar zoom tamanho poliline
		if (polyline != null) {
    			LatLngBounds.Builder builder = new LatLngBounds.Builder();
			for (LatLng point : polyline.getPoints()) { builder.include(point); }
			builder.include(latLng);
			bounds = builder.build();
			int padding = 100;

			//zoom = calculateZoomfrom(latLng, polyline);
			zoom = (float) 15;
		} else { zoom = (float) 15; }
    		
		// mover camera atualizar marcadorez
    		CameraPosition cameraPosition = new CameraPosition.Builder().target(latLng).zoom(zoom).build();
    		//googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
		googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
	}
	}
}

//calcular zoon poliline
public float calculateZoomfrom(LatLng latLng, Polyline polyline) {
    LatLngBounds.Builder builder = new LatLngBounds.Builder();
    for (LatLng point : polyline.getPoints()) {
        builder.include(point);
    }
    builder.include(latLng);
    LatLngBounds bounds = builder.build();
    int padding = 100; 
    int width = getResources().getDisplayMetrics().widthPixels;
    int height = getResources().getDisplayMetrics().heightPixels;
    float zoom = googleMap.getCameraPosition().zoom; 

    if (googleMap != null) {
        googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, width, height, padding));
        zoom = googleMap.getCameraPosition().zoom;
    }
    return zoom;
}


/*


//inicializacao mapa 
@Override
public void onMapReady(GoogleMap googleMap) {
    this.googleMap = googleMap;


    LatLng initialPosition = latLng; 
    CameraPosition cameraPosition = new CameraPosition.Builder()
            .target(initialPosition)
            .zoom(14)
            .bearing(0) 
            .tilt(0) 
            .build();

    googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

    // Bloquear a rotação do mapa
    googleMap.getUiSettings().setRotateGesturesEnabled(false);

    // Adicionar marcadores e configurações adicionais
    oriIcon = BitmapDescriptorFactory.fromBitmap(resizeBitmap(R.drawable.ini, oriMarkerWidth, oriMarkerHeight));
    desIcon = BitmapDescriptorFactory.fromBitmap(resizeBitmap(R.drawable.fim, desMarkerWidth, desMarkerHeight));
    carIcon = BitmapDescriptorFactory.fromBitmap(resizeBitmap(R.drawable.xar, carMarkerWidth, carMarkerHeight));
    transparentIcon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED);
    carMarker = googleMap.addMarker(new MarkerOptions().position(initialPosition).anchor(0.5f, 0.5f).icon(carIcon));
    xMarker = googleMap.addMarker(new MarkerOptions().position(initialPosition).anchor(0.5f, 0.5f).icon(transparentIcon));
    oriMarker = googleMap.addMarker(new MarkerOptions().position(new LatLng(-33.869, 151.208)).anchor(0.5f, 0.5f).icon(oriIcon));
    desMarker = googleMap.addMarker(new MarkerOptions().position(new LatLng(-33.870, 151.209)).anchor(0.5f, 0.5f).icon(desIcon));

    carMarker.setVisible(true);
    xMarker.setVisible(true);
    oriMarker.setVisible(true);
    desMarker.setVisible(true);


	

    //habilitar selecao ponto destino noca rota
    googleMap.setOnCameraMoveStartedListener(new GoogleMap.OnCameraMoveStartedListener() {
        public void onCameraMoveStarted(int reason) {
            if (reason == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE) {
                //mapaCentralizar = false;
		//checkloc.setChecked(false);
            }
        }
    });

}


*/




//inicializacao mapa
@Override
public void onMapReady(GoogleMap googleMap) {
    this.googleMap = googleMap;

    LatLng initialPosition = latLng;
    CameraPosition cameraPosition = new CameraPosition.Builder()
            .target(initialPosition)
            .zoom(14)
            .bearing(0)
            .tilt(0)
            .build();

    googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

   // cameraPosition = new CameraPosition.Builder().target(latLng).zoom(15).build();

    // Bloquear a rotação do mapa
    googleMap.getUiSettings().setRotateGesturesEnabled(false);

    // Adicionar marcadores e configurações adicionais
    oriIcon = BitmapDescriptorFactory.fromBitmap(resizeBitmap(R.drawable.ini, oriMarkerWidth, oriMarkerHeight));
    desIcon = BitmapDescriptorFactory.fromBitmap(resizeBitmap(R.drawable.fim, desMarkerWidth, desMarkerHeight));
    carIcon = BitmapDescriptorFactory.fromBitmap(resizeBitmap(R.drawable.xar, carMarkerWidth, carMarkerHeight));
    transparentIcon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED);
    carMarker = googleMap.addMarker(new MarkerOptions().position(initialPosition).anchor(0.5f, 0.5f).icon(carIcon));
    xMarker = googleMap.addMarker(new MarkerOptions().position(initialPosition).anchor(0.5f, 0.5f).icon(transparentIcon));
    oriMarker = googleMap.addMarker(new MarkerOptions().position(new LatLng(-33.869, 151.208)).anchor(0.5f, 0.5f).icon(oriIcon));
    desMarker = googleMap.addMarker(new MarkerOptions().position(new LatLng(-33.870, 151.209)).anchor(0.5f, 0.5f).icon(desIcon));

    carMarker.setVisible(true);
    xMarker.setVisible(false);
    oriMarker.setVisible(true);
    desMarker.setVisible(true);



    //habilitar selecao ponto destino noca rota
    googleMap.setOnCameraMoveStartedListener(new GoogleMap.OnCameraMoveStartedListener() {
        public void onCameraMoveStarted(int reason) {
            if (reason == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE) {
                mapaCentralizar = false;
		checkloc.setChecked(false);
            }
        }
    });
}


//procesar marcadores
private Bitmap resizeBitmap(int resourceId, int width, int height) {
		
	Bitmap imageBitmap = BitmapFactory.decodeResource(getResources(), resourceId);
	return Bitmap.createScaledBitmap(imageBitmap, width, height, false);
}



//minimizar inicuar minimapa
@Override
protected void onPause() {
    super.onPause();
    mapView.onPause();
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        if (!Settings.canDrawOverlays(MainActivity.this)) {
            Intent oIntent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
	    Uri.parse("package:" + getPackageName()));
            startActivityForResult(oIntent, 1234); 
	    // 1234 é um código de solicitação arbitrário
        } else {
           //startOverlayService();
        }
    } else {
        //startOverlayService();
    }
}


//fechar overlay
private void stopOverlayService() {
    Intent oIntent = new Intent(MainActivity.this, OverlayService.class);
    stopService(oIntent);
}


//fechar aplicativo
@Override
public void onDestroy() {      
	super.onDestroy();   
	//stopFakeLoc();
	//mapView.onDestroy();
	//

}                                  

//baixa memoria android
public void onLowMemory() {
	super.onLowMemory();
	mapView.onLowMemory();
}

@Override
    protected void onStop() {
        super.onStop();
	saveTurboValue(turbo);
    }



//////fimmmmm 
}
