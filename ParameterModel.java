/**
 * 
 */
package dionasys.holon.datamodel;

/**
 * @author elhabbash
 *
 */

public class ParameterModel {

    String name;    
    Integer dataType;

    public ParameterModel(String name, Integer dataType) {
        this.name = name;
        this.dataType = dataType;
    }

    public String getName() {
        return name;
    }
   
    public int getDataType() {
        return dataType;
    }

    public void setName(String name) {
        this.name = name;    
    }
    
    public void setDataType(Integer dataType) {
        this.dataType = dataType;
    }

    @Override
    public String toString(){
        return "name = " + name + "\n"                
                + "dataType = " + dataType ;                
    }
}