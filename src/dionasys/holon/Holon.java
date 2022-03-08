/**
 * 
 */
package dionasys.holon;

import java.io.IOException;
import java.rmi.registry.Registry;
import java.util.ArrayList;

import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import dionasys.holon.datamodel.HolonDataModel;
import dionasys.holon.datamodel.OntologyCreator;

/**
 * @author elhabbash
 *
 */

public class Holon {

   private HolonDataModel holonDataModel;    
   private String type;
   protected HolonRegistry holonsRegistry;
   
   private OWLOntology receivedOntology;    
   private OWLOntology ontologyModel;
   private Table<String,HolonDataModel,Double> servicesTree = HashBasedTable.create();

   public Holon(HolonDataModel model, String type, HolonRegistry registry) {
       this.holonDataModel = model;    
       this.type = type;
       this.holonsRegistry = registry;   
       createOntology();
       registry.registerHolon(ontologyModel, type);
   }
   
   public void createOntology(){
       try {
           ontologyModel = (new OntologyCreator()).creatOntology(holonDataModel);            
       } catch (OWLOntologyCreationException | OWLOntologyStorageException | IOException ex) {
           ex.printStackTrace();            
       }
   }
   
   public void reRegister(){
	   holonsRegistry.registerHolon(ontologyModel, type);
   }
       
   public ArrayList<OWLOntology> lookupHolon(String type){
       return holonsRegistry.getHolon(type);
   }
   
   public HolonDataModel getHolonDataModel(){
       return holonDataModel;
   }
   
   public OWLOntology getOntologyModel(){
       return ontologyModel;
   }
   
   public Table getServicesTree(){
       return servicesTree;
   }
   
   public void printServicesTree() {
       for (String serviceName : servicesTree.rowKeySet()) {
           for (HolonDataModel holonModel : servicesTree.columnKeySet()) {            
               System.out.println(serviceName + " " + holonModel.getName() + " " + servicesTree.get(serviceName, holonModel));
           }
       }
   }
}