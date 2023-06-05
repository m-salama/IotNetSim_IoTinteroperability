 /** To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dionasys.holon.datamodel;

import java.util.HashMap;
import java.util.ArrayList;

/**
 *
 * @author elhabbas
 */

public class HolonDataModel {

	private HashMap<String,String> data;
	private ArrayList<HolonServiceModel> services;
   
    public HolonDataModel() {
    	data = new HashMap<>();
    }
    
    public void putData(String key, String value) {
    	data.put(key, value);
    }
    
    public String getData(String key) {
    	return data.get(key);
    }
    
    public ArrayList<HolonServiceModel> getServices(){
    	return services;
    }
    
    public HashMap<String,String> getAllData(){
    	return  data;
    }
    
    public void setAllData(HashMap<String,String> data){
    	this.data = data;
    }
    
    public void setServices(ArrayList<HolonServiceModel> services) {
		this.services = services;
	}
    
    public HolonServiceModel getServiceByAnnotation(int annotation) {
    	for(HolonServiceModel ser : services) {
    		if(ser.getAnnotation() == annotation) {
    			return ser;
    		}
    	}
    	return null;
    }

	@Override
    public String toString() {
    	String str = "";
    	for(String key:data.keySet()) {
    		str += key + ":" + data.get(key) + "\n";
    	}
    	str += "services are : \n"; 
    	for(HolonServiceModel ser : services) {
    		str += ser.toString() + "\n";
    	}
    	return str;
    }
    
}
