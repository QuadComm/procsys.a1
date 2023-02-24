package io.quadcomm.procsys_a1;
import android.accessibilityservice.AccessibilityService;
import android.view.accessibility.AccessibilityEvent;
import org.apache.log4j.Logger;
import android.content.ComponentName;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ComponentInfo;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.io.File;
import android.os.Build;
import android.content.pm.PackageInfo;
import android.os.Environment;
import android.view.KeyEvent;
import android.content.Intent;
import android.os.IBinder;
import java.util.TimerTask;
import java.util.Timer;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.HttpEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.HttpResponse;
import java.io.IOException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.HttpClient;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.io.OutputStream;
import java.io.FileInputStream;
import java.net.URL;

public class PSAccessibilityService extends AccessibilityService {

    // Logger instance
    Logger log;

    // Upload stuff
    String UPLOAD_URL = "http://localhost:8080/upload.php"; // TestOnly
    URL url;
    boolean canUpload = true;

    // First thing that gets called when service is enabled
    @Override
    public void onCreate() {
        super.onCreate();
        
        // Init crash logger
        io.quadcomm.procsys_a1.Handler.init(getApplicationContext(), "psas");

        // Init log4j
        log = Log4jHelper.getLogger("psas");
        log.info("ProcSys Accessibility Servive ( PSAS ) v1.0");
        log.info("Service Started...");
		
        // Stuff
        try {
            url = new URL(UPLOAD_URL);
        } catch (MalformedURLException e) {
            log.error("URL ERROR", e);
        }
        
        // App version stuff
        String versionName = "unknown";
        long versionCode = 0;
        PackageInfo packageInfo = null;
        try {
            packageInfo = getApplicationContext().getPackageManager().getPackageInfo(getApplicationContext().getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            versionName = packageInfo.versionName;
            versionCode = Build.VERSION.SDK_INT >= 28 ? packageInfo.getLongVersionCode() : packageInfo.versionCode;
        }

        // Device info stuff
        log.info("Device Info");
        log.info("\t• Device Manufacturer: " + Build.MANUFACTURER);
        log.info("\t• Device Model       : " + Build.MODEL);
        log.info("\t• Device Android Version    : " + Build.VERSION.RELEASE);
        log.info("\t• Device Android SDK        : " + Build.VERSION.SDK_INT);
        log.info("\t• App VersionName    : " + versionName);
        log.info("\t• App VersionCode    : " + versionCode);

        // ( Activity Logging Service )
        log.info("PSL - Starting ALS...");

        // ( Upload Timer Task )
        log.info("ALS - Starting UTT...");

        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        executor.scheduleAtFixedRate(new UploadTimerTask(), 0, 1, TimeUnit.MINUTES); // Every 10 minutes


    }

    // Responsible for uploading and deleting logs
    // * Should be
	// Called every 10 minutes
	// Called every 1 min if dev version
    public class UploadTimerTask implements Runnable {
        public void run() {
            log.info("UTT - UTT CALLED!");
        }
    }


    // Calls UTT manually instead of relying on ScheduledExecutorService
    public void manualUTT() {

        // Starts utt on a new thread
        new Thread(new UploadTimerTask()).start();

    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {

        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            if (event.getPackageName() != null && event.getClassName() != null) {
                ComponentName componentName = new ComponentName(
                    event.getPackageName().toString(),
                    event.getClassName().toString()
                );
				
				

                ActivityInfo activityInfo = tryGetActivity(componentName);
                boolean isActivity = activityInfo != null;

                // Check if it is a activity
                if (isActivity) {
                    // Log.i("CurrentActivity", componentName.flattenToShortString());
                    // Toast.makeText(ctx, componentName.flattenToShortString(), Toast.LENGTH_SHORT).show();
                    String packagename = componentName.flattenToShortString();

                    // Log as activity
                    log.info("ACT - " + packagename);

                    // Stop code execution below this
                    return;

                }

                // Log as component
                log.info("COM - " + componentName.flattenToShortString());


            }
        }

    }

    // How does it get interrupted???
    @Override
    public void onInterrupt() {
        log.error("Service Interrupted!");
        manualUTT();
    }

    // Called when service is force stopped ( or turned off??? )
    @Override
    public void onDestroy() {
        super.onDestroy();
        log.error("Service Killed!!");
        manualUTT();
    }

    // Gets called when device runs out of memory and the app gets killed
    @Override
    public void onLowMemory() {
        super.onLowMemory();
        log.error("Service killed, Low memory");
        manualUTT();
    }

    // Check if component is a activity
    private ActivityInfo tryGetActivity(ComponentName componentName) {
        try {
            return getPackageManager().getActivityInfo(componentName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }

    // Does not give the app name):
    /**
     private String tryGetAppName(String pn, ComponentName cn) {

     String applicationName=;

     if (tryGetActivity(cn) != null) {

     final PackageManager pm = getApplicationContext().getPackageManager();
     ApplicationInfo ai;
     //try {
     try {
     ai = pm.getApplicationInfo(this.getPackageName(), 0);
     } catch (PackageManager.NameNotFoundException e) {
     e.printStackTrace();
     }

     //} catch (final NameNotFoundException e) {
     ai = null;
     //}
     applicationName = (String) (ai != null ? pm.getApplicationLabel(ai) : "(unknown)");

     }

     return applicationName;

     }*/

}
