import java.io.*;
import java.util.*;

public class ReadProperty {
    private final static ReadProperty INSTANCE = new ReadProperty();
    
    static public ReadProperty getInstance() {
        return INSTANCE;
    }
    
    public String getValue(String filePatch, String key) {
        try {
            File f = new File(filePatch);
            if (f.exists()) {
                Properties pro = new Properties();
                FileInputStream in = new FileInputStream(f);
                pro.load(in);

                String p = pro.getProperty(key);
                return p;
            } else {
                System.out.println("File not found!");
                return null;
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
            return null;
        }
    }
}