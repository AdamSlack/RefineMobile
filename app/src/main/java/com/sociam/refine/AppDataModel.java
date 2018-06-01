package com.sociam.refine;

import android.arch.core.util.Function;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.os.AsyncTask;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

import javax.net.ssl.HttpsURLConnection;

public class AppDataModel {
    private static AppDataModel INSTANCE = null;

    private HashMap<String, XRayApp> xRayApps;
    private HashMap<String, AppInfo> allPhoneAppInfos;
    private HashMap<String, AppInfo> trackedPhoneAppInfos;

    private AppDataModel(PackageManager packageManager, Context context) {
        xRayApps = new HashMap<String, XRayApp>();
        allPhoneAppInfos = getAppInfos(packageManager);
        trackedPhoneAppInfos = new HashMap<>(allPhoneAppInfos);

        for(String key : allPhoneAppInfos.keySet()) {
            requestXRayAppData(allPhoneAppInfos.get(key).getAppPackageName(), context, new Function<XRayApp, Void>() {
                @Override
                public Void apply(XRayApp app) {
                    xRayApps.put(app.app, app);
                    return null;
                }
            });
        }
    }

    public static AppDataModel getInstance(PackageManager packageManager, Context context) {
        if(INSTANCE == null) {
            INSTANCE = new AppDataModel(packageManager, context);
        }
        return INSTANCE;
    }

    private List<ResolveInfo> getAppList(PackageManager packageManager) {
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        return packageManager.queryIntentActivities( mainIntent, 0);
    }

    private HashMap<String, AppInfo> getAppInfos(PackageManager packageManager) {
        List<ResolveInfo> appList = getAppList(packageManager);
        HashMap<String, AppInfo> phoneAppInfos = new HashMap<>();
        for(ResolveInfo app : appList) {
            AppInfo appInfo = new AppInfo(app, packageManager);
            phoneAppInfos.put(appInfo.getAppPackageName(), appInfo);
        }
        return phoneAppInfos;
    }

    private void requestXRayAppData(final String packageName, final Context context, final Function<XRayApp, Void> useXRayAppCallback ) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    String xrayAPIString = context.getResources().getString(R.string.xray_apps);
                    URL APIEndpoint = new URL(xrayAPIString + "?appId=" + packageName+ "&isFull=true");
                    HttpsURLConnection httpsURLConnection = (HttpsURLConnection) APIEndpoint.openConnection();

                    httpsURLConnection.setRequestProperty("User-Agent", "com.refine.sociam");
                    httpsURLConnection.setRequestProperty("Accept", "application/json");

                    if (httpsURLConnection.getResponseCode() == 200) {
                        InputStream responseBody = httpsURLConnection.getInputStream();
                        InputStreamReader responseBodyReader = new InputStreamReader(responseBody, "UTF-8");
                        XRayJsonReader xrayReader = new XRayJsonReader();
                        List<XRayApp> apps = xrayReader.readAppArray(responseBodyReader);
                        if (apps.size() == 0) {
                            useXRayAppCallback.apply(new XRayApp(packageName, new XRayAppStoreInfo("Unknown", "Unknown")));
                        }
                        else {
                            for (XRayApp app : apps) {
                                useXRayAppCallback.apply(app);
                            }
                        }

                    } else {
                        // Failed to connect
                        useXRayAppCallback.apply(new XRayApp(packageName, new XRayAppStoreInfo("Unknown", "Unknown")));
                    }
                    httpsURLConnection.disconnect();

                } catch (MalformedURLException exc) {
                    // URL was Dodge

                } catch (IOException exc) {
                    // IO Failed here.
                }
                finally {

                }
            }
        });
    }

    /**
     *
     * Getters
     *
     */
    public HashMap<String, AppInfo> getAllPhoneAppInfos() {
        return allPhoneAppInfos;
    }

    public HashMap<String, AppInfo> getTrackedPhoneAppInfos() {
        return trackedPhoneAppInfos;
    }

    public HashMap<String, XRayApp> getxRayApps() {
        return xRayApps;
    }
}