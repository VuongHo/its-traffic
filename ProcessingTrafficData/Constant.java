public class Constant {
    // Constant for config file
    // static public String MEMCACHED_CONFIG_FILE  = System.getProperty("configPath") + "/memcached_server_config.properties";
    static public String MEMCACHED_CONFIG_FILE  = "doc/memcached_server_config.properties";

    // Constant for memcached;
    static public String HOST                   = "HOST";
    static public String WEIGHTS                = "WEIGHTS";
    static public String INIT_CONN              = "INIT_CONN";
    static public String MIN_CONN               = "MIN_CONN";
    static public String MAX_CONN               = "MAX_CONN";
    static public String MAINT_SLEEP            = "MAINT_SLEEP";
    static public String NAGLE                  = "NAGLE";
    static public String MAX_IDLE               = "MAX_IDLE";
    static public String SOCKET_TO              = "SOCKET_TO";
    static public String SOCKET_CONNECT_TO      = "SOCKET_CONNECT_TO"; 

    // Congfig for application
    static public String APPLICATION_CONFIG_FILE  = "doc/application_server_config.properties";
    static public Double ROOT_CELL_LATITUDE       = Double.parseDouble(ReadProperty.getInstance().getValue(APPLICATION_CONFIG_FILE, "ROOT_CELL_LATITUDE"));
    static public Double ROOT_CELL_LONGITUDE      = Double.parseDouble(ReadProperty.getInstance().getValue(APPLICATION_CONFIG_FILE, "ROOT_CELL_LONGITUDE"));
    static public Double DISTANCE_CELL            = Double.parseDouble(ReadProperty.getInstance().getValue(APPLICATION_CONFIG_FILE, "DISTANCE_CELL"));
    static public Double DISTANCE_ONE_DEGREE      = Double.parseDouble(ReadProperty.getInstance().getValue(APPLICATION_CONFIG_FILE, "DISTANCE_ONE_DEGREE"));
 
    static public String WIDTH_OF_STREET          = ReadProperty.getInstance().getValue(APPLICATION_CONFIG_FILE, "WIDTH_OF_STREET");
    static public Double ANPHA_OF_STREET          = Double.parseDouble(ReadProperty.getInstance().getValue(APPLICATION_CONFIG_FILE, "ANPHA_OF_STREET"));

    static public Boolean GENNERATE_VIRTUAL_DATA  = Boolean.parseBoolean(ReadProperty.getInstance().getValue(APPLICATION_CONFIG_FILE, "GENNERATE_VIRTUAL_DATA"));
    static public Boolean WRITE_GPS_NOT_MATCH     = Boolean.parseBoolean(ReadProperty.getInstance().getValue(APPLICATION_CONFIG_FILE, "WRITE_GPS_NOT_MATCH"));

    static public Boolean PRINT_LOG               = Boolean.parseBoolean(ReadProperty.getInstance().getValue(APPLICATION_CONFIG_FILE, "PRINT_LOG"));

    static public int TIMER                       = Integer.parseInt(ReadProperty.getInstance().getValue(APPLICATION_CONFIG_FILE, "TIMER"));
    static public int LAST_MINUTE                 = Integer.parseInt(ReadProperty.getInstance().getValue(APPLICATION_CONFIG_FILE, "LAST_MINUTE"));

    static public Boolean GET_ALL_SEGMENT         = Boolean.parseBoolean(ReadProperty.getInstance().getValue(APPLICATION_CONFIG_FILE, "GET_ALL_SEGMENT"));

    static public int EXPIRING_TIME_CACHE         = Integer.parseInt(ReadProperty.getInstance().getValue(APPLICATION_CONFIG_FILE, "EXPIRING_TIME_CACHE"));
    static public int TIME_INSERT_NEW_FRAME       = Integer.parseInt(ReadProperty.getInstance().getValue(APPLICATION_CONFIG_FILE, "TIME_INSERT_NEW_FRAME"));
    
    static public int LIMIT_RECORDS               = Integer.parseInt(ReadProperty.getInstance().getValue(APPLICATION_CONFIG_FILE, "LIMIT_RECORDS"));
}