package io.quadcomm.procsys_a1;
import de.mindpipe.android.logging.log4j.LogConfigurator;
import android.os.Environment;
import org.apache.log4j.Logger;
import android.content.Context;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import android.os.Build;

public class Log4jHelper {

    private final static LogConfigurator mLogConfigrator = new LogConfigurator();
    
    public static Context filepath;
    
    static {
        configureLog4j();
    }

    private static void configureLog4j() {
        
		String dirName=null;
		
        if (Build.VERSION.SDK_INT >= 30) { //Android R. AIDE didn't support Build.VERSION_CODES.R
			dirName = "/storage/emulated/0/Documents/";
		} else {
			dirName = String.valueOf(filepath.getExternalFilesDir(null));
		}
        String log_pattern = "%d - [%c] - %p : %m%n";
        int maxBackupSize = 10;
        long maxFileSize = 1024 * 4096;
        
        // Logs directory
        String dateandtime = new SimpleDateFormat("yyyy_MM_dd-HH_mm_ss").format(new Date());
        String filename_fl = "psas_al_shane_" + dateandtime + ".psl";
        
        File filepath1 = new File(dirName, filename_fl);
		
        try {
            if(!filepath1.exists())
            filepath1.createNewFile();
        } catch (IOException e) {}

        configure( filepath1.getAbsolutePath(), log_pattern, maxBackupSize, maxFileSize );
        
    }

    private static void configure( String fileName, String filePattern, int maxBackupSize, long maxFileSize ) {
        mLogConfigrator.setFileName( fileName );
        mLogConfigrator.setMaxFileSize( maxFileSize );
        mLogConfigrator.setFilePattern(filePattern);
        mLogConfigrator.setMaxBackupSize(maxBackupSize);
        mLogConfigrator.setUseLogCatAppender(true);
        mLogConfigrator.configure();

    }

    public static Logger getLogger( String name ) {
        org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger( name );
        return logger;
    }
}
