public class Constant {

    // Congfig for application
    static public String APPLICATION_CONFIG_FILE  = "doc/gpslogger_server_config.properties";
    static public Double LATITUDE_MAX      = Double.parseDouble(ReadProperty.getInstance().getValue(APPLICATION_CONFIG_FILE, "LATITUDE_MAX"));
    static public Double LATITUDE_MIN      = Double.parseDouble(ReadProperty.getInstance().getValue(APPLICATION_CONFIG_FILE, "LATITUDE_MIN"));
    static public Double LONGITUDE_MAX     = Double.parseDouble(ReadProperty.getInstance().getValue(APPLICATION_CONFIG_FILE, "LONGITUDE_MAX"));
    static public Double LONGITUDE_MIN     = Double.parseDouble(ReadProperty.getInstance().getValue(APPLICATION_CONFIG_FILE, "LONGITUDE_MIN"));
}