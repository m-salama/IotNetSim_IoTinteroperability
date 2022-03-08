/**
 * 
 */
package dionasys.holon;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.semanticweb.owlapi.model.OWLOntology;

/**
 * @author elhabbash
 *
 */
public class HolonRegistry {

    Map<OWLOntology,String> lstHolons;
    
    public HolonRegistry(){
        lstHolons = new ConcurrentHashMap<>();
    }
    
    public ArrayList<OWLOntology> getHolon(String type){
        ArrayList<OWLOntology> lst = new ArrayList<>();
        for (OWLOntology entry : lstHolons.keySet()){
            if(lstHolons.get(entry).equalsIgnoreCase(type)){
                lst.add(entry);
            }
        }
        return lst;
    }
    
    public void registerHolon(OWLOntology model, String type){
    	lstHolons.put(model, type);
    }

}
