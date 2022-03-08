/**
 * 
 */
package dionasys.holon.datamodel;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author elhabbash
 *
 */

@XmlRootElement(name = "parameter")
public class ParameterModel {

    String name;    
    String dataType;

    public ParameterModel(String name, String dataType) {
        this.name = name;
        this.dataType = dataType;
    }

    public String getName() {
        return name;
    }
   
    public String getDataType() {
        return dataType;
    }

    @XmlElement
    public void setName(String name) {
        this.name = name;    
    }
    
    @XmlElement
    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    @Override
    public String toString(){
        return "name = " + name + "\n"                
                + "dataType = " + dataType ;                
    }
}