/**
 * 
 */
package dionasys.holon.datamodel;

import java.util.ArrayList;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author elhabbash
 *
 */

@XmlRootElement(name = "service")
public class HolonServiceModel {

    String name;
    ArrayList<ParameterModel> parameters;
    String url;
    double cost;
    String returnType;

    public String getName() {
        return name;
    }
    
   public ArrayList<ParameterModel> getParameters() {
        return parameters;
    }
   
    public String getUrl() {
        return url;
    }
    
    public double getCost() {
        return cost;
    }
    
    public String getReturnType() {
        return returnType;
    }

    @XmlElement
    public void setName(String name) {
        this.name = name;    
    }
    
    @XmlElement
    public void setParameters(ArrayList<ParameterModel> parameters) {
        this.parameters = parameters;
    }
    
    @XmlElement
    public void setUrl(String url) {
        this.url = url;
    }

    @XmlElement
    public void setCost(double cost) {
        this.cost = cost;
    }  
    
    @XmlElement
    public void setReturnType(String returnType) {
        this.returnType = returnType;
    } 
    
    @Override
    public String toString(){
        return "name = " + name + ", "
                + "url = " + url + ", "
                + "cost = " + cost + ", "
                + "parameters = " + parameters + ", "
                + "returns = " + returnType;
    }
    
    /**
     * returns true if the two services are the same (i.e. same service provided by the same holon)
     * @return 
     */
    public boolean isEqual(HolonServiceModel serviceModelToCheck){
        return name.equals(serviceModelToCheck.getName()) && url.equals(serviceModelToCheck.getUrl());
    }
    
    /**
     * returns true if the two services provide the same functionality but by different holons 
     * @return 
     */
    public boolean isSameType(HolonServiceModel ser){
        return name.equals(ser.getName());
    }
}