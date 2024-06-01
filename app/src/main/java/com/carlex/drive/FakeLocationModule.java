package com.carlex.drive;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Handler;
import android.util.Log;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;




public class FakeLocationModule implements IXposedHookLoadPackage {

    private Context context;
    private Location interceptedLocation;
    private Location interceptedLocationNetwork;



    @Override
public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {

    // Obter contexto da aplicação principal
    context = MainActivity.mainApp;

    // Lista dos pacotes desejados
    String[] desiredPackages = new String[]{
            "com.carlex.drive",
            "com.app99.drive",
            "flar2.devcheck",
            "com.google.android.apps.location.gps.gnsslogger",
            "fr.dvilleneuve.lockito"
    };

    //inicio do loop 
    // Verificar se o pacote atual está na lista dos pacotes desejados
    for (String packageName : desiredPackages) {
        if (lpparam.packageName.equals(packageName)) {

	  //inicio gps provider
            // Hookar o método setGpsProvider na classe xLocationManager
            XposedHelpers.findAndHookMethod(
                "com.carlex.drive.xLocationManager",
                // Nome completo da classe onde o método está localizado
                lpparam.classLoader,
                // ClassLoader do pacote sendo hookado
                "setGpsProvider",
                // Nome do método a ser hookado
                Location.class,
                // Tipo do parâmetro do método
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(
		    MethodHookParam param) throws Throwable {
                        interceptedLocation = (Location) param.args[0];
                        if (LocationManager.GPS_PROVIDER
			.equals(interceptedLocation.getProvider())) {
                            Log.d("FakeLocationModule", 
			    "Intercepted GPS Location: " +
			    interceptedLocation.toString());
                        }
                    }
                }
            );

            // Hook nos métodos do LocationManager gps provider
            for (Method method : 
		LocationManager.class.getDeclaredMethods()) {
                if (method.getName().equals("requestLocationUpdates")
                        && !Modifier.isAbstract(method.getModifiers())
                        && Modifier.isPublic(method.getModifiers())) {
                    XposedBridge.hookMethod(method, new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod
			(MethodHookParam param) throws Throwable {
                            log("requestLocationUpdates called");
                            if (param.args.length >= 4 
				&& (param.args[3] instanceof
				LocationListener)) {
                                replaceInstanceMethod(param.args[3], 
				"onLocationChanged", new XC_MethodHook() {
                                    @Override
                                    protected void 
				    beforeHookedMethod(MethodHookParam 
				    param) throws Throwable {
                                        log("onLocationChanged called");
                                        if (interceptedLocation != null) {
                                            param.args[0] = interceptedLocation;
                                        }
                                    }
                                });
                            }
                        }
                    });
                } else if (method.getName().equals("requestSingleUpdate")
                        && !Modifier.isAbstract(method.getModifiers())
                        && Modifier.isPublic(method.getModifiers())) {
                    XposedBridge.hookMethod(method, new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(
			MethodHookParam param) throws Throwable {
                            log("requestSingleUpdate called");
                            if (param.args.length >= 3 && (
			    param.args[1] instanceof LocationListener)) {
                                replaceInstanceMethod(param.args[1], 
				"onLocationChanged", new XC_MethodHook() {
                                    @Override
                                    protected void beforeHookedMethod
				    (MethodHookParam param) throws 
				    Throwable {
                                        log("onLocationChanged called");
                                        if (interceptedLocation != null) {
                                            param.args[0] = interceptedLocation;
                                        }
                                    }
                                });
                            }
                        }
                    });
                } else if (method.getName().equals("getLastKnownLocation")
                        && !Modifier.isAbstract(method.getModifiers())
                        && Modifier.isPublic(method.getModifiers())) {
                    XposedBridge.hookMethod(method, new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam 
			param) throws Throwable {
                            log("getLastKnownLocation called");
                            if (interceptedLocation != null) {
                                param.setResult(interceptedLocation);
                            }
                        }
                    });
		}
            }
	    //fim gps provider

	    //inicio network provider
	    // Hookar o método setNetworkProvider na classe xLocationManager
	    XposedHelpers.findAndHookMethod(
		"com.carlex.drive.xLocationManager",
		// Nome completo da classe onde o método está localizado
		lpparam.classLoader,
		// ClassLoader do pacote sendo hookado
		"setNetworkProvider",
		// Nome do método a ser hookado
		Location.class,
		// Tipo do parâmetro do método
		new XC_MethodHook() {
		    @Override
		    protected void beforeHookedMethod(
		    MethodHookParam param) throws Throwable {
			interceptedLocationNetwork = (Location) param.args[0];		
			if (LocationManager.NETWORK_PROVIDER		
			.equals(interceptedLocationNetwork.getProvider())) {
			    Log.d("FakeLocationModule", 
			    "Intercepted Network Location: " +
			    interceptedLocationNetwork.toString());

			}
		    }
		}	
	    );

	    // Hook nos métodos do LocationManager network provider
	    for (Method method :
		LocationManager.class.getDeclaredMethods()) {
		    if (method.getName().equals("requestLocationUpdates")
			    && !Modifier.isAbstract(method.getModifiers())
			    && Modifier.isPublic(method.getModifiers())) {
			XposedBridge.hookMethod(method, new XC_MethodHook() {
				@Override
				protected void beforeHookedMethod
				(MethodHookParam param) throws Throwable {
				    log("requestLocationUpdates called");
				    if (param.args.length >= 4
				    && (param.args[3] instanceof
				    LocationListener)) {
					replaceInstanceMethod(param.args[3],
					"onLocationChanged", new XC_MethodHook() {
					    @Override
					    protected void
					    beforeHookedMethod(MethodHookParam
					    param) throws Throwable {
						log("onLocationChanged called");
						if (interceptedLocationNetwork != null) {
						    param.args[0] = interceptedLocationNetwork;
						}
					    }
					});
				    }
				}
			});
		    } else if (method.getName().equals("requestSingleUpdate")
			    && !Modifier.isAbstract(method.getModifiers())
			    && Modifier.isPublic(method.getModifiers())) {
			XposedBridge.hookMethod(method, new XC_MethodHook() {
				@Override
				protected void beforeHookedMethod(
				MethodHookParam param) throws Throwable {
				    log("requestSingleUpdate called");
				    if (param.args.length >= 3 && (
				    param.args[1] instanceof LocationListener)) {
					replaceInstanceMethod(param.args[1],
					"onLocationChanged", new XC_MethodHook() {
					    @Override
					    protected void beforeHookedMethod
					    (MethodHookParam param) throws
					    Throwable {
						log("onLocationChanged called");
						if (interceptedLocationNetwork != null) {
						    param.args[0] = interceptedLocationNetwork;
						}
					    }
					});
				    }
				}
			});
		    } else if (method.getName().equals("getLastKnownLocation")
			    && !Modifier.isAbstract(method.getModifiers())
			    && Modifier.isPublic(method.getModifiers())) {
			XposedBridge.hookMethod(method, new XC_MethodHook() {
				@Override
				protected void afterHookedMethod(MethodHookParam
				param) throws Throwable {
				    log("getLastKnownLocation called");
				    if (interceptedLocationNetwork != null) {
					param.setResult(interceptedLocationNetwork);
				    }
				}
			});
		    }
		}
	    }
	    //fim network provider
	}
    }

    //log
    private void log(String message) {                                        Log.d("FakeLocationModule", message);                             }                                        


    //replaceInstanceMethod            
    private void replaceInstanceMethod(Object instance, String methodName, XC_MethodHook hook) {        
	for (Method method : instance.getClass().
	getDeclaredMethods()) {                                   
		if (method.getName().equals(methodName) && !Modifier.isAbstract(method.getModifiers()) && Modifier.isPublic(method.getModifiers())) {                   
			XposedBridge.hookMethod(method, hook);     
	    	}                             
	}                                                      
    }                                                                                                                                           //end      
}



