
package com.carlex.drive;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;

public class fl implements IXposedHookLoadPackage {

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (lpparam.packageName.equals("com.carlex.drive")) {
            return;
        }

        // Acesse as preferências do módulo Xposed
        XSharedPreferences pref = new XSharedPreferences("com.carlex.drive", "xtest_preference");
        pref.makeWorldReadable();
        boolean testPref = pref.getBoolean("xtest_preference", true);

        // Faça algo com a preferência
        XposedBridge.log("Test preference value: " + testPref);
    }
}
