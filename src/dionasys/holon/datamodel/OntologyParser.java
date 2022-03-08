package dionasys.holon.datamodel;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.ConsoleProgressMonitor;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerConfiguration;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.SimpleConfiguration;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;

/**
 * @author elhabbash
 *
 */
public class OntologyParser {

	private static OWLOntologyManager manager = null;    
    private static OWLDataFactory dataFactory = null;
    private static OWLReasoner reasoner = null;
    private static ConcurrentHashMap<String, String> instancesClassesMap = new ConcurrentHashMap<>(); //key = instance, value = class
    private static HolonDataModel holon;
    private static IRI ontologyIRI;

    public static HolonDataModel parse(InputStream inputStream) throws OWLOntologyCreationException {
        manager = OWLManager.createOWLOntologyManager();
        OWLOntology holonOntology = manager.loadOntologyFromOntologyDocument(inputStream);  
        //System.out.println("parsing = " + holonOntology.getOWLOntologyManager().getOntologyDocumentIRI(holonOntology));
        dataFactory = manager.getOWLDataFactory();       
        //ontologyIRI = IRI.create("http://Dionasys"); 
        //manager.setOntologyDocumentIRI(holonOntology, ontologyIRI);        
        return parse(holonOntology);
    }
    
    public static HolonDataModel parse(OWLOntology holonOntology) throws OWLOntologyCreationException {        
        manager = holonOntology.getOWLOntologyManager();
        holon = new HolonDataModel();        
        dataFactory = manager.getOWLDataFactory();
        ontologyIRI = manager.getOntologyDocumentIRI(holonOntology);
        
        OWLReasonerFactory reasonerFactory = new StructuralReasonerFactory();
        ConsoleProgressMonitor progressMonitor = new ConsoleProgressMonitor();        
        OWLReasonerConfiguration config = new SimpleConfiguration(progressMonitor);
        reasoner = reasonerFactory.createReasoner(holonOntology, config);
        
        OWLClass holonID = dataFactory.getOWLClass(IRI.create(ontologyIRI + "#HolonID"));
        holon.setName(getDataPropertyValues(getSingleIndividual(holonID),holonOntology));
        
        OWLClass holonIP = dataFactory.getOWLClass(IRI.create(ontologyIRI + "#HolonIP"));
        holon.setAddress(getDataPropertyValues(getSingleIndividual(holonIP),holonOntology));
        
        OWLClass holonMessagingProtocol = dataFactory.getOWLClass(IRI.create(ontologyIRI + "#MessagingProtocol"));
        holon.setMessagingProtocol(getDataPropertyValues(getSingleIndividual(holonMessagingProtocol),holonOntology));
        
        OWLClass holonLocation = dataFactory.getOWLClass(IRI.create(ontologyIRI + "#Location")); //e.g. room1, office10
        holon.setLocation(getDataPropertyValues(getSingleIndividual(holonLocation),holonOntology));
        
        OWLClass holonLatitude = dataFactory.getOWLClass(IRI.create(ontologyIRI + "#Latitude"));
        holon.setLatitude(getDataPropertyValues(getSingleIndividual(holonLatitude),holonOntology));
        
        OWLClass holonLongitude = dataFactory.getOWLClass(IRI.create(ontologyIRI + "#Longitude"));
        holon.setLongitude(getDataPropertyValues(getSingleIndividual(holonLongitude),holonOntology));
        
        OWLClass holonMobility = dataFactory.getOWLClass(IRI.create(ontologyIRI + "#Mobility"));
        holon.setMobility(getDataPropertyValues(getSingleIndividual(holonMobility),holonOntology));
        
        OWLClass holonTemperature = dataFactory.getOWLClass(IRI.create(ontologyIRI + "#Temperature"));
        holon.setTemperature(getDataPropertyValues(getSingleIndividual(holonTemperature),holonOntology));
        
        OWLClass holonReliabilityProfile = dataFactory.getOWLClass(IRI.create(ontologyIRI + "#Reliability_Profile"));
        holon.setReliability(getDataPropertyValues(getSingleIndividual(holonReliabilityProfile),holonOntology));
        
        OWLClass holonServices = dataFactory.getOWLClass(IRI.create(ontologyIRI + "#Service"));
        holon.setServices(getHolonServices(holonServices, holonOntology));
        return holon;
    }

        
    /**
     * returns the literal of the individual
     *
     * @param individual
     * @return
     */
    private static String getDataPropertyValues(OWLNamedIndividual individual, OWLOntology holonOntology) {
        Stream<OWLDataPropertyAssertionAxiom> ax = holonOntology.dataPropertyAssertionAxioms(individual);
        Iterator<OWLDataPropertyAssertionAxiom> iter = ax.iterator();
        while (iter.hasNext()) {
            OWLDataPropertyAssertionAxiom axiom = iter.next();
            OWLLiteral literal = axiom.getObject();
            return literal.getLiteral();
        }
        return null;
    }

    /**
     * returns the class of the linked property
     *
     * @param individual
     * @return
     */
    private static String getObjectPropertyValues(OWLNamedIndividual individual, OWLOntology holonOntology) {
        Stream<OWLObjectPropertyAssertionAxiom> ax = holonOntology.objectPropertyAssertionAxioms(individual);        
        Iterator<OWLObjectPropertyAssertionAxiom> iter = ax.iterator();
        if (iter.hasNext()) {
            OWLObjectPropertyAssertionAxiom axiom = iter.next();
            Stream<OWLNamedIndividual> stream = axiom.individualsInSignature();
            Iterator<OWLNamedIndividual> compIter = stream.iterator();
            while (compIter.hasNext()) {
                OWLNamedIndividual comp = compIter.next();                
                if (!comp.getIRI().getIRIString().equals(individual.getIRI().getIRIString())) {
                    return comp.getIRI().getShortForm();
//                    if (instancesClassesMap.containsKey(comp.getIRI().getShortForm())) {                        
//                        return instancesClassesMap.get(comp.getIRI().getShortForm());
//                    }
                }
            }
        }
        return null;
    }

    /**
     * Classifies the parameter to either data or object property
     *
     * @param individual
     * @return
     */
    private static String classifyParameter(OWLNamedIndividual individual, OWLOntology holonOntology) {
        Stream<OWLDataPropertyAssertionAxiom> axData = holonOntology.dataPropertyAssertionAxioms(individual);
        long dataCount = axData.count();
        Stream<OWLObjectPropertyAssertionAxiom> axObject = holonOntology.objectPropertyAssertionAxioms(individual);
        long objectCount = axObject.count();        
        if (dataCount > 0 && objectCount == 0) {
            return "data";
        } else if (objectCount > 0 && dataCount == 0) {
            return "object";
        } else {
            return "";
        }
    }

    /**
     * returns the HolonModel Parameters
     *
     * @param individual
     * @return
     */
    private static ArrayList<OWLNamedIndividual> getHolonParameters(OWLNamedIndividual individual, OWLOntology holonOntology) {
        ArrayList<OWLNamedIndividual> parameters = new ArrayList<>();
        Stream<OWLObjectPropertyAssertionAxiom> axObject = holonOntology.objectPropertyAssertionAxioms(individual);
        Iterator<OWLObjectPropertyAssertionAxiom> iterObject = axObject.iterator();
        while (iterObject.hasNext()) {
            OWLObjectPropertyAssertionAxiom axiomObject = iterObject.next();
            if (axiomObject.containsEntityInSignature(dataFactory.getOWLObjectProperty(ontologyIRI + "#hasParameter"))) {
                Stream stream = axiomObject.individualsInSignature();
                Iterator<OWLNamedIndividual> compIter = stream.iterator();
                while (compIter.hasNext()) {
                    OWLNamedIndividual comp = compIter.next();
                    if (!comp.getIRI().getIRIString().equals(individual.getIRI().getIRIString())) {
                        parameters.add(comp);
                    }
                }
            }
        }
        return parameters;
    }

    /**
     * returns the HolonModel services
     *
     * @param holonServices
     * @param holonOntology
     * @return ArrayList<Service>
     */
    private static ArrayList<HolonServiceModel> getHolonServices(OWLClass holonServices, OWLOntology holonOntology) {
        NodeSet<OWLNamedIndividual> individuals = reasoner.getInstances(holonServices, false);
        Iterator<OWLNamedIndividual> iterIndividual = individuals.entities().iterator();
        ArrayList<HolonServiceModel> services = new ArrayList<>();
        while (iterIndividual.hasNext()) {
        	HolonServiceModel ser = new HolonServiceModel();
            OWLNamedIndividual individual = iterIndividual.next();   
            //objects --> parameters
            Stream<OWLObjectPropertyAssertionAxiom> axObject = holonOntology.objectPropertyAssertionAxioms(individual);            
            Iterator<OWLObjectPropertyAssertionAxiom> iter = axObject.iterator();
            ArrayList<ParameterModel> parameters = new ArrayList<>();
            String returnDataType = "";
            while (iter.hasNext()) {
                OWLObjectPropertyAssertionAxiom o = iter.next();
                if (o.containsEntityInSignature(dataFactory.getOWLObjectProperty(ontologyIRI + "#hasParameter"))) {
                    Stream stream = o.individualsInSignature();
                    Iterator<OWLNamedIndividual> compIter = stream.iterator();
                    //find the service parameters
                    while (compIter.hasNext()) {
                        OWLNamedIndividual comp = compIter.next();
                        if (!comp.getIRI().getIRIString().equals(individual.getIRI().getIRIString())) {
                            String parameterName = comp.getIRI().getShortForm().split("_")[2];
                            Stream<OWLDataPropertyAssertionAxiom> ax = holonOntology.dataPropertyAssertionAxioms(comp);
                            Iterator<OWLDataPropertyAssertionAxiom> iter2 = ax.iterator();
                            while (iter2.hasNext()) {
                                OWLDataPropertyAssertionAxiom axiom = iter2.next();
                                OWLLiteral literal = axiom.getObject();
                                String parameterDataType = literal.getDatatype().toStringID().split("#")[1];
                                parameters.add(new ParameterModel(parameterName, parameterDataType));
                            }
                        }
                    }//while (compIter.hasNext()
                } else if (o.containsEntityInSignature(dataFactory.getOWLObjectProperty(ontologyIRI + "#hasReturn"))) {
                    Stream stream = o.individualsInSignature();                    
                    Iterator<OWLNamedIndividual> compIter = stream.iterator(); 
                    while (compIter.hasNext()) {                        
                        OWLNamedIndividual comp = compIter.next();                        
                        if (!comp.getIRI().getIRIString().equals(individual.getIRI().getIRIString())) {                             
                            Stream<OWLDataPropertyAssertionAxiom> ax = holonOntology.dataPropertyAssertionAxioms(comp);                            
                            Iterator<OWLDataPropertyAssertionAxiom> iter2 = ax.iterator();                            
                            if (iter2.hasNext()) {
                                OWLDataPropertyAssertionAxiom axiom = iter2.next();                                
                                OWLLiteral literal = axiom.getObject();                                                             
                                returnDataType = literal.getDatatype().toString().split("#")[1];                                    
                            }                                                     
                        }
                    } 
                }
                ser.setParameters(parameters);
                ser.setReturnType(returnDataType);
            }   
            //Data --> attirutes: name, cost, return 
            Stream<OWLDataPropertyAssertionAxiom> axData = holonOntology.dataPropertyAssertionAxioms(individual);            
            Iterator<OWLDataPropertyAssertionAxiom> iterData = axData.iterator();
            //printStream(axData);
            while (iterData.hasNext()) {
                OWLDataPropertyAssertionAxiom axiom = iterData.next();
                OWLLiteral literal = axiom.getObject();
                String value = literal.getLiteral();
                String key = "";
                Stream<OWLDataProperty> dataStream = axiom.dataPropertiesInSignature();            
                Iterator<OWLDataProperty> iterData2 = dataStream.iterator();
                if (iterData2.hasNext()) {
                    OWLDataProperty prop = iterData2.next();
                    key = prop.getIRI().getShortForm();
                }
                if(key.equals("name")){
                    ser.setName(value);
                }else if(key.equals("url")){
                    ser.setUrl(value);
                }else if(key.equals("cost")){
                    ser.setCost(Double.parseDouble(value));
                }
            }

            services.add(ser);
        }//while (iterIndividual.hasNext())
        return services;
    }

    /**
     * print a stream
     *
     * @param stream
     */
    private static void printStream(Stream stream) {
        Iterator iter = stream.iterator();
        int i=0;
        while (iter.hasNext()) {
            Object o = iter.next();
            System.out.println(o);
            i++;
        }
        System.out.println("stream size = " + i);
    }

   
    /**
     * get the individual of a class
     * @param clazz 
     */
    private static OWLNamedIndividual getSingleIndividual(OWLClass clazz) {
        NodeSet<OWLNamedIndividual> instances = reasoner.getInstances(clazz, false);
        Iterator<OWLNamedIndividual> iterIndividual = instances.entities().iterator();
        if (iterIndividual.hasNext()) {
            return iterIndividual.next();
        }
        return null;
    }
   
    /**
     * Finds all the instances in the ontology and stores them with the their
     * classes in a map key = instances (in a short form), value = class (in a
     * short form)
     */
    private static void parseInstances(OWLOntology holonOntology) {
        Stream<OWLClass> classes = holonOntology.classesInSignature();
        Iterator<OWLClass> iter = classes.iterator();
        while (iter.hasNext()) {
            OWLClass clazz = iter.next();
            NodeSet<OWLNamedIndividual> instances = reasoner.getInstances(clazz, false);
            Iterator<OWLNamedIndividual> iterIndividual = instances.entities().iterator();
            while (iterIndividual.hasNext()) {
                OWLNamedIndividual i = iterIndividual.next();
                instancesClassesMap.put(i.getIRI().getShortForm(), clazz.getIRI().getShortForm());               
            }
        }
    }

    /**
     * Assign extracted values to the holon object
     *
     * @param shortForm
     * @param value
     */
    private static void assignValueToHolonParameter(String shortForm, String value) {
        String parameter = shortForm.split("-")[1];        
        switch (parameter) {
            case "id":
                holon.setName(value);
                break;
            case "ip":
                holon.setAddress(value);
                break;
            case "mobility":
                holon.setMobility(value);
                break;
            case "reliability":
                holon.setReliability(value);
                break;
            case "temperature":
                holon.setTemperature(value);
                break;
            case "messagingProtocol":
                holon.setMessagingProtocol(value);
                break;
            default:
                break;
        }
    }
 
    /**
     * 
     * @param shortForm
     * @param value 
     */
    private static void assignValueToFunctionParameter(String shortForm, String value, HolonServiceModel service) {        
        switch (shortForm) {
            case "name":
                service.setName(value);
                break;
            case "url":
                service.setUrl(value);
                break;
            case "parameters":
                String[] para = value.split(",");
                ArrayList<String> arr = new ArrayList<>();
                arr.addAll(Arrays.asList(para));
                //service.setParameters(arr);
                break;
            case "cost":
                service.setCost(Double.parseDouble(value));                            
                break;
            default:
                break;
        }
    }       
}