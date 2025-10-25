// File: EmulatorLogger.java
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class EmulatorLogger {
    private static PrintWriter logWriter;
    private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    
    static {
        try {
            logWriter = new PrintWriter(new FileWriter("nes_emulator.log", true));
            log("=== SESSÃO INICIADA ===");
        } catch (IOException e) {
            System.err.println("Não foi possível criar arquivo de log: " + e.getMessage());
        }
    }
    
    public static void log(String message) {
        String timestamp = dateFormat.format(new Date());
        String logMessage = "[" + timestamp + "] " + message;
        
        System.out.println(logMessage);
        if (logWriter != null) {
            logWriter.println(logMessage);
            logWriter.flush();
        }
    }
    
    public static void close() {
        if (logWriter != null) {
            log("=== SESSÃO FINALIZADA ===");
            logWriter.close();
        }
    }
}