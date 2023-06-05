/**
 * 
 */
package dionasys.holon;

import java.util.Iterator;
import java.util.Map;

import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.iotnetsim.iot.nodes.holon.IoTNodeHolon;
import org.semanticweb.owlapi.model.OWLOntology;

import com.google.common.collect.HashBasedTable;

/**
 * @author elhabbash
 *
 */
public class HolonRegistry {

    
	HashBasedTable<String,String,OWLOntology> lstHolons; //<type, name,ontology>
    
    public HolonRegistry(){
        lstHolons = HashBasedTable.create();
    }
    
    public OWLOntology getHolonByType(String type){
       	Map<String,OWLOntology> candidateHolons = lstHolons.row(type);
    	Iterator<String> iter = candidateHolons.keySet().iterator();
    	if(iter.hasNext()) {
    		return candidateHolons.get(iter.next());
    	}
    	return null;
    }
    
    public OWLOntology getHolonByName(String name){
       	Map<String,OWLOntology> candidateHolons = lstHolons.column(name);
    	Iterator<String> iter = candidateHolons.keySet().iterator();
    	if(iter.hasNext()) {
    		return candidateHolons.get(iter.next());
    	}
    	return null;
    }
    
    public void registerHolon(String type, String node, OWLOntology model){
    	lstHolons.put(type, node, model);
    	Log.printLine(CloudSim.clock() + ": Holon " + node + " registered" );
    }

}
