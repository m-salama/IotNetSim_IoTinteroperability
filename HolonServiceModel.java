/**
 * 
 */
package dionasys.holon.datamodel;

import java.util.ArrayList;

/**
 * @author elhabbash
 *
 */

public class HolonServiceModel {

    String name;
    ArrayList<ParameterModel> parameters;
    String url;
    double cost;
    int returnType;
    int annotation;

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
    
    public int getReturnType() {
        return returnType;
    }

    
    public void setName(String name) {
        this.name = name;    
    }
    
    
    public void setParameters(ArrayList<ParameterModel> parameters) {
        this.parameters = parameters;
    }
    
    public void setUrl(String url) {
        this.url = url;
    }

    
    public void setCost(double cost) {
        this.cost = cost;
    }  
    
    
    public void setReturnType(int returnType) {
        this.returnType = returnType;
    } 
    
    @Override
    public String toString(){
        return "name = " + name + ", "
                + "url = " + url + ", "
                + "cost = " + cost + ", "
                + "parameters = " + parameters + ", "
                + "returns = " + returnType + ", "
                + "annotation = " + annotation;
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

	public int getAnnotation() {
		return annotation;
	}

	public void setAnnotation(int annotation) {
		this.annotation = annotation;
	}
    
    
}