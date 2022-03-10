package dionasys.holon.datamodel;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLDeclarationAxiom;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

import com.scalified.tree.TraversalAction;
import com.scalified.tree.TreeNode;

/**
 * @author elhabbash
 *
 */
public class OntologyCreator {

	private OWLOntology holonOntology = null;
	private OWLDataFactory dataFactory = null;
	private OWLOntologyManager manager = null;
	private IRI ontologyIRI = null;

	public OWLOntology creatOntology(HolonDataModel holon)
			throws OWLOntologyCreationException, OWLOntologyStorageException, FileNotFoundException, IOException {

		// String holonTimestamp = holon.getTimestamp();
		ArrayList<HolonServiceModel> holonServices = holon.getServices();

		manager = OWLManager.createOWLOntologyManager();
		holonOntology = manager.createOntology();
		dataFactory = manager.getOWLDataFactory();
		ontologyIRI = IRI.create("http://Dionasys");
		manager.setOntologyDocumentIRI(holonOntology, ontologyIRI);

		// classes
		OWLClass holonClass = createOWLClass("Holon");
		OWLClass leafHolon = createOWLClass("Leaf_Holon");
		OWLClass parentHolon = createOWLClass("Parent_Holon");
		OWLClass rootHolon = createOWLClass("Root_Holon");
		OWLClass holonParameters = createOWLClass("HolonParameters");
		OWLClass holonID = createOWLClass("HolonID");
		OWLClass holonIP = createOWLClass("HolonIP");
		OWLClass holonMessagingProtocol = createOWLClass("MessagingProtocol");
		OWLClass holonLocation = createOWLClass("Location");
		OWLClass holonLatitude = createOWLClass("Latitude");
		OWLClass holonLongitude = createOWLClass("Longitude");
		OWLClass holonMobility = createOWLClass("Mobility");
		OWLClass environmentalConditions = createOWLClass("EnvironmentalConditions");
		OWLClass temperature = createOWLClass("Temperature");
		OWLClass serviceClass = createOWLClass("Service");
		OWLClass profile = createOWLClass("Profile");
		OWLClass preferenceProfile = createOWLClass("Preference_Profile");
		OWLClass reliabilityProfile = createOWLClass("Reliability_Profile");
		OWLClass serviceProperties = createOWLClass("Service_Properties");
		OWLClass routingProperties = createOWLClass("Routing_Properties");
		OWLClass reliability = createOWLClass("Reliability");
		OWLClass parameter = createOWLClass("Parameter");
		OWLClass returnClass = createOWLClass("Return");

		// declare sub classes
		declareSubClass(leafHolon, holonClass);
		declareSubClass(parentHolon, holonClass);
		declareSubClass(rootHolon, holonClass);
		declareSubClass(holonID, holonParameters);
		declareSubClass(holonIP, holonParameters);
		declareSubClass(holonMessagingProtocol, holonParameters);
		declareSubClass(holonLocation, holonParameters);
		declareSubClass(holonLatitude, holonParameters);
		declareSubClass(holonLongitude, holonParameters);
		declareSubClass(holonMobility, holonParameters);
		declareSubClass(temperature, environmentalConditions);
		declareSubClass(preferenceProfile, profile);
		declareSubClass(reliabilityProfile, preferenceProfile);
		declareSubClass(routingProperties, serviceProperties);
		declareSubClass(reliability, routingProperties);

		// individuals
		OWLNamedIndividual holonIndividual = createIndividual("holonIndividual");
		OWLNamedIndividual holonIDIndividual = createIndividual("holonID");
		OWLNamedIndividual holonIPIndividual = createIndividual("holonIP");
		OWLNamedIndividual holonParametersIndividual = createIndividual("holonParameters");
		OWLNamedIndividual holonMessagingProtocolIndividual = createIndividual("messagingProtocol");
		OWLNamedIndividual holonLocationIndividual = createIndividual("location");
		OWLNamedIndividual holonLatitudeIndividual = createIndividual("latitude");
		OWLNamedIndividual holonLongitudeIndividual = createIndividual("longitude");
		OWLNamedIndividual mobilityIndividual = createIndividual("mobility");
		OWLNamedIndividual environmentalConditionsIndividual = createIndividual("environmentalConditions");
		OWLNamedIndividual temperatureIndividual = createIndividual("temperature");
		OWLNamedIndividual profileIndividual = createIndividual("profile");
		OWLNamedIndividual preferenceProfileIndividual = createIndividual("preferenceProfile");
		OWLNamedIndividual reliabilityProfileIndividual = createIndividual("reliabilityProfile");
		OWLNamedIndividual servicePropertiesIndividual = createIndividual("serviceProperties");
		OWLNamedIndividual routingPropertiesIndividual = createIndividual("routingProperties");
		OWLNamedIndividual reliabilityIndividual = createIndividual("reliabilityProperties");

		// declare Indiviual Types
		declareIndiviualType(holonClass, holonIndividual);
		declareIndiviualType(holonID, holonIDIndividual);
		declareIndiviualType(holonIP, holonIPIndividual);
		declareIndiviualType(holonParameters, holonParametersIndividual);
		declareIndiviualType(holonMessagingProtocol, holonMessagingProtocolIndividual);
		declareIndiviualType(holonLocation, holonLocationIndividual);
		declareIndiviualType(holonLatitude, holonLatitudeIndividual);
		declareIndiviualType(holonLongitude, holonLongitudeIndividual);
		declareIndiviualType(holonMobility, mobilityIndividual);
		declareIndiviualType(environmentalConditions, environmentalConditionsIndividual);
		declareIndiviualType(temperature, temperatureIndividual);
		declareIndiviualType(profile, profileIndividual);
		declareIndiviualType(preferenceProfile, preferenceProfileIndividual);
		declareIndiviualType(reliabilityProfile, reliabilityProfileIndividual);
		declareIndiviualType(serviceProperties, servicePropertiesIndividual);
		declareIndiviualType(routingProperties, routingPropertiesIndividual);
		declareIndiviualType(reliability, reliabilityIndividual);

		// holon hasParameter
//        addObjectAxiom("hasParameter", holonIndividual, holonIDIndividual);
//        addObjectAxiom("hasParameter", holonIndividual, holonIPIndividual);
//        addObjectAxiom("hasParameter", holonIndividual, holonMobilityIndividual);
//        addObjectAxiom("hasParameter", holonIndividual, holonTemperatureIndividual);
//        addObjectAxiom("hasParameter", holonIndividual, holonReliabilityIndividual);
//        addObjectAxiom("hasParameter", holonIndividual, holonMessagingProtocolIndividual);

		// holon parameters' values
		addDataAxiom("hasValue", holon.getName() + "", holonIDIndividual, "string");
		addDataAxiom("hasValue", holon.getAddress() + "", holonIPIndividual, "string");
		addDataAxiom("hasValue", holon.getMessagingProtocol() + "", holonMessagingProtocolIndividual, "string");
		addDataAxiom("hasValue", holon.getLocation() + "", holonLocationIndividual, "string");
		addDataAxiom("hasValue", holon.getLatitude() + "", holonLatitudeIndividual, "string");
		addDataAxiom("hasValue", holon.getLongitude() + "", holonLongitudeIndividual, "string");
		addDataAxiom("hasValue", holon.getMobility() + "", mobilityIndividual, "string");
		addDataAxiom("hasValue", holon.getTemperature() + "", temperatureIndividual, "string");
		if (holon.getReliability() != null) {
			addDataAxiom("hasValue", holon.getReliability(), reliabilityProfileIndividual, "string");
		}

		// reliability profile
//        if (holonReliability.equalsIgnoreCase("BestEffort")) {
//            OWLNamedIndividual bestEffortIndividual = null;
//            OWLClass bestEffortClass = dataFactory.getOWLClass(IRI.create(ontologyIRI + "#Best_Effort"));
//            NodeSet<OWLNamedIndividual> instances = reasoner.getInstances(bestEffortClass, false);
//            Iterator<OWLNamedIndividual> iterIndividual = instances.entities().iterator();
//            if (iterIndividual.hasNext()) {
//                bestEffortIndividual = iterIndividual.next();
//                addObjectAxiom("hasProfile", holonReliabilityIndividual, bestEffortIndividual);
//            }
//        }
//
		for (HolonServiceModel service : holonServices) {
			OWLNamedIndividual serviceIndividual = createIndividual("serviceIndividual_" + service.getName());
			declareIndiviualType(serviceClass, serviceIndividual);
			addDataAxiom("name", service.getName(), serviceIndividual, "string");
			addDataAxiom("cost", service.getCost() + "", serviceIndividual, "string");
			addDataAxiom("url", service.getUrl(), serviceIndividual, "string");
//          addDataAxiom("returns", service.getReturns() + "", serviceIndividual, "boolean");

			for (ParameterModel par : service.getParameters()) {
				OWLNamedIndividual parameterIndividual = createIndividual(
						serviceIndividual.getIRI().getShortForm() + "_" + par.getName());
				declareIndiviualType(parameter, parameterIndividual);
				// addDataAxiom("name", par.getName() , parameterIndividual, "string");
				addDataAxiom(par.getName(), par.getDataType(), parameterIndividual, par.getDataType());
				addObjectAxiom("hasParameter", serviceIndividual, parameterIndividual);
			}

			OWLNamedIndividual returnIndividual = createIndividual(
					serviceIndividual.getIRI().getShortForm() + "_return");
			declareIndiviualType(returnClass, returnIndividual);
			addDataAxiom("return", service.getReturnType(), returnIndividual, service.getReturnType());
			addObjectAxiom("hasReturn", serviceIndividual, returnIndividual);

			addObjectAxiom("providesService", holonIndividual, serviceIndividual);
		}
//          File holonFile = new File("holons/" + holonId + ".owl");
//          FileOutputStream out = new FileOutputStream(holonFile);
		// manager.saveOntology(holonOntology);
		saveOntologyToDesk(holonOntology, holon.getName());
		// System.out.println(holonOntology);
		return holonOntology;
	}

	/**
	 * Create ontology of the holon tree
	 * 
	 * @param tree
	 * @return
	 * @throws org.semanticweb.owlapi.model.OWLOntologyCreationException
	 * @throws org.semanticweb.owlapi.model.OWLOntologyStorageException
	 * @throws java.io.FileNotFoundException
	 */
	/*
	 * public OWLOntology creatOntology(TreeNode<NodeObject> tree) throws
	 * OWLOntologyCreationException, OWLOntologyStorageException,
	 * FileNotFoundException, IOException { HolonDataModel me = (HolonDataModel)
	 * tree.root().data().getObject(); TraversalAction<TreeNode<NodeObject>> action
	 * = new TraversalAction<TreeNode<NodeObject>>() {
	 * 
	 * @Override public void perform(TreeNode<NodeObject> node) { NodeObject obj =
	 * node.data(); if (node.isLeaf()) { HolonServiceModel service =
	 * (HolonServiceModel) node.data().getObject(); me.getServices().add(service); }
	 * }
	 * 
	 * @Override public boolean isCompleted() { return false; // return true in
	 * order to stop traversing } }; tree.traversePreOrder(action); return
	 * creatOntology(me); }
	 */
	
	/**
	 * Create OWLClass
	 * 
	 * @param clazz
	 * @return
	 */
	private OWLClass createOWLClass(String clazz) {
		OWLClass owlClass = dataFactory.getOWLClass(ontologyIRI + "#" + clazz);
		OWLDeclarationAxiom ax = dataFactory.getOWLDeclarationAxiom(owlClass);
		manager.addAxiom(holonOntology, ax);
		return owlClass;
	}

	/**
	 * declareSubClass
	 * 
	 * @param child
	 * @param parent
	 */
	private void declareSubClass(OWLClass child, OWLClass parent) {
		OWLSubClassOfAxiom ax = dataFactory.getOWLSubClassOfAxiom(child, parent);
		manager.applyChange(new AddAxiom(holonOntology, ax));
	}

	/**
	 * 
	 * @param ind
	 * @return individual
	 */
	private OWLNamedIndividual createIndividual(String ind) {
		OWLNamedIndividual owlInd = dataFactory.getOWLNamedIndividual(ontologyIRI + "#" + ind);
		OWLDeclarationAxiom ax = dataFactory.getOWLDeclarationAxiom(owlInd);
		manager.addAxiom(holonOntology, ax);
		return owlInd;
	}

	/**
	 * Create OWLClassAssertionAxiom
	 *
	 * @param clazz
	 * @param ind
	 * @return
	 */
	private void declareIndiviualType(OWLClass owlClass, OWLNamedIndividual individual) {
		// OWLClass owlClass = dataFactory.getOWLClass(IRI.create(ontologyIRI + "#" +
		// clazz));
		// OWLNamedIndividual individual =
		// dataFactory.getOWLNamedIndividual(IRI.create(ontologyIRI + "#" + ind));
		OWLClassAssertionAxiom ax = dataFactory.getOWLClassAssertionAxiom(owlClass, individual);
		manager.addAxiom(holonOntology, ax);
		// return individual;
	}

	/**
	 * Create and add ObjectAxiom
	 *
	 * @param property
	 * @param ind1
	 * @param ind2
	 * @param ontologyIRI
	 */
	private void addObjectAxiom(String property, OWLNamedIndividual ind1, OWLNamedIndividual ind2) {
		OWLObjectProperty objectProperty = dataFactory.getOWLObjectProperty(ontologyIRI + "#" + property);
		OWLObjectPropertyAssertionAxiom objectAxiom = dataFactory.getOWLObjectPropertyAssertionAxiom(objectProperty,
				ind1, ind2);
		manager.addAxiom(holonOntology, objectAxiom);
	}

	/**
	 * Create and add data property axiom
	 *
	 * @param property
	 * @param value
	 * @param ind
	 * @param ontologyIRI
	 */
	private void addDataAxiom(String property, String value, OWLNamedIndividual ind, String dataType) {
		OWLDatatype owlDatatype = null;
		if (dataType.equalsIgnoreCase("string")) {
			owlDatatype = dataFactory.getStringOWLDatatype();
		} else if (dataType.equalsIgnoreCase("boolean")) {
			owlDatatype = dataFactory.getBooleanOWLDatatype();
		} else if (dataType.equalsIgnoreCase("integer")) {
			owlDatatype = dataFactory.getIntegerOWLDatatype();
		}
		OWLDataProperty dataProperty = dataFactory.getOWLDataProperty(ontologyIRI + "#" + property);
		OWLLiteral literal = dataFactory.getOWLLiteral(value, owlDatatype);
		OWLDataPropertyAssertionAxiom holonIDAxiom = dataFactory.getOWLDataPropertyAssertionAxiom(dataProperty, ind,
				literal);
		manager.addAxiom(holonOntology, holonIDAxiom);
	}

	/**
	 *
	 * @param ontology
	 * @param holonId
	 * @throws FileNotFoundException
	 * @throws OWLOntologyStorageException
	 */
	public void saveOntologyToDesk(OWLOntology ontology, String holonId)
			throws FileNotFoundException, OWLOntologyStorageException {
		File holonFile = new File("test/" + holonId + ".owl");
		FileOutputStream out = new FileOutputStream(holonFile);
		manager.saveOntology(ontology, out);
	}

}
