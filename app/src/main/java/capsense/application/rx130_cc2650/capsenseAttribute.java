package capsense.application.rx130_cc2650;

import java.util.HashMap;
import java.util.UUID;

public class capsenseAttribute {

    private static HashMap<String, String> gattAttributes = new HashMap();
    
    public static String RX130_CC2650_SERVICE = "0000FFF0-0000-1000-8000-00805F9B34FB";
    public static String GREEN_LED = "0000FFF1-0000-1000-8000-00805F9B34FB";
    public static String YELLOW_LED = "0000FFF2-0000-1000-8000-00805F9B34FB";
    public static String RED_LED = "0000FFF3-0000-1000-8000-00805F9B34FB";
    public static String LED_CHANGE = "0000FFF4-0000-1000-8000-00805F9B34FB";
    public final static UUID UUID_CHARACTERISTIC_UPDATE_NOTIFICATION_DESCRIPTOR = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"); // UUID for notification descriptor
    public final static UUID UUID_LED_CHANGE = UUID.fromString(LED_CHANGE);




    static {
        // Services
        gattAttributes.put(RX130_CC2650_SERVICE.toLowerCase(), "RX130 CC2650 Service");
        // Characteristics
        gattAttributes.put(GREEN_LED.toLowerCase(), "Green_LED");
        gattAttributes.put(YELLOW_LED.toLowerCase(), "Yellow_LED");
        gattAttributes.put(RED_LED.toLowerCase(), "Red_LED");
        gattAttributes.put(LED_CHANGE.toLowerCase(), "LED Change");


    }

    /**
     * Search the map for the attribute name of a given UUID
     *
     * @param uuid        UUID to search for
     * @param defaultName Name to return if the UUID is not found in the map
     *
     * @return Name of attribute with given UUID
     */
    public static String lookup(String uuid, String defaultName) {
        String name = gattAttributes.get(uuid);
        return name == null ? defaultName : name;
    }

    /**
     * @return Map of UUIDs and attribute names used in the Project Zero demo
     */
    public static HashMap<String, String> gattAttributes(){
        return gattAttributes;
    }

}
