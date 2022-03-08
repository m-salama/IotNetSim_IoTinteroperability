/**
 * 
 */
package dionasys.holon.datamodel;

import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author elhabbash
 *
 */
@XmlRootElement(name = "holon")
public class HolonDataModel {

    private String name;
    private String address;
    private int port;
    private ArrayList<HolonServiceModel> services;
    private String timestamp;
    private String mobility;
    private String reliability;
    private String temperature;
    private String messagingProtocol;
    private String batteryLevel;
    private String type;
    private String id;    
    private String latitude;
    private String longitude;
    private String menu;
    private String dayAvailable;
    private String timeAvailable;
    private String location;
    
    
    OntologyCreator ontologyCreator;
    DatagramSocket socket;
    byte[] receivedOntology = new byte[2000];
    
    
    public HolonDataModel() {
        ontologyCreator = new OntologyCreator();
        try {
            socket = new DatagramSocket();
        } catch (SocketException ex) {
            ex.printStackTrace();
        }
    }
    
    public String getMessagingProtocol() {
        return messagingProtocol;
    }

    public void setMessagingProtocol(String messagingProtocol) {
        this.messagingProtocol = messagingProtocol;
    }   

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public int getPort() {
        return port;
    }

    public ArrayList<HolonServiceModel> getServices() {
        return services;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getMobility() {
        return mobility;
    }

    public String getReliability() {
        return reliability;
    }

    public String getTemperature() {
        return temperature;
    }
    
    public String getLocation(){
        return location;
    }

    public void setLocation(String location){
        this.location = location;
    }
    
    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }
    
    


    public void setName(String name) {
        this.name = name;
    }


    public void setAddress(String address) {
        this.address = address;
    }


    public void setPort(int port) {
        this.port = port;
    }


    public void setServices(ArrayList<HolonServiceModel> services) {
        this.services = services;
    }


    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }


    public void setMobility(String mobility) {
        this.mobility = mobility;
    }


    public void setReliability(String reliability) {
        this.reliability = reliability;
    }

    public void setTemperature(String temperature) {
        this.temperature = temperature;
    }
    
    @Override
    public String toString() {
        return "name = " + name + "\n"
                + "address = " + address + "\n"
                + "port = " + port + "\n"                
                + "timestamp = " + timestamp + "\n"
                + "mobility = " + mobility + "\n"
                + "reliability = " + reliability + "\n"
                + "temperature = " + temperature + "\n"
                + "messagingProtocol = " + messagingProtocol + "\n"
                + "location = " + latitude + "-" + longitude + "\n"
                + "services :\n " + servicesToString() + "\n";        
    }
    
    private String servicesToString(){
        String str="";
        for(HolonServiceModel ser : services){
            str += ser.toString() + "\n";
        }
        return str;
    }    
}
