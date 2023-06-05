/**
 * 
 */
package dionasys.holon;

import java.io.IOException;

import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

import dionasys.holon.datamodel.HolonDataModel;
import dionasys.holon.datamodel.OntologyCreator;

/**
 * @author elhabbash
 *
 */

public class Holon{

   protected HolonDataModel holonDataModel;    
   protected HolonRegistry holonsRegistry;
   protected int forwardNodeId;
   
   protected OWLOntology receivedOntology;    
   protected OWLOntology ontologyModel;

   /*
   public Holon(HolonDataModel model, String type, HolonRegistry registry) {
       this.holonDataModel = model;  
       this.type = type;
       this.holonsRegistry = registry;   
       createOntology();
       registry.registerHolon(ontologyModel, type);
   }
   */
   
   public Holon() {   
   }
   
   public Holon(HolonDataModel model) {
       this.holonDataModel = model;  
       //createOntology();
       //register holon
   }
   
   public void createOntology(){
       try {
           ontologyModel = (new OntologyCreator()).creatOntology(holonDataModel);            
       } catch (OWLOntologyCreationException | OWLOntologyStorageException | IOException ex) {
           ex.printStackTrace();            
       }
   }
   
   public void reRegister(String name){
	   holonsRegistry.registerHolon(this.getHolonDataModel().getData("type"), 
			   name, ontologyModel);
   }
       
   public void setHolonDataModel(HolonDataModel dataModel){
       this.holonDataModel = dataModel;
   }
   
   public HolonDataModel getHolonDataModel(){
       return holonDataModel;
   }
   
   public OWLOntology getOntologyModel(){
       return ontologyModel;
   }
}