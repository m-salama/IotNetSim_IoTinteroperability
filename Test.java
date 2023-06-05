package dionasys.holon;

import java.util.ArrayList;

import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import dionasys.holon.datamodel.*;

public class Test {

	public static void main(String args[]) throws OWLOntologyCreationException {

		HolonDataModel holon1Model = new HolonDataModel();
		holon1Model.putData("name", "holon1");
		holon1Model.putData("type", "car");

		HolonServiceModel holon1Service1 = new HolonServiceModel();
		holon1Service1.setName("service1");
		holon1Service1.setCost(0);
		holon1Service1.setUrl("http://holon1service1URL");
		holon1Service1.setReturnType(Types.BOOLEAN);
		ArrayList<ParameterModel> holon1Service1Parameters = new ArrayList<>();
		holon1Service1Parameters.add(new ParameterModel("speed", Types.STRING));
		holon1Service1Parameters.add(new ParameterModel("color", Types.STRING));
		holon1Service1.setParameters(holon1Service1Parameters);
		ArrayList<HolonServiceModel> holon1Services = new ArrayList<>();
		holon1Services.add(holon1Service1);
		holon1Model.setServices(holon1Services);

		Holon holon1 = new Holon(holon1Model);		
		HolonDataModel holon2Model = new HolonDataModel();
		holon2Model.putData("name", "holon2");
		holon2Model.putData("type","car");
		HolonServiceModel holon2Service1 = new HolonServiceModel();
		holon2Service1.setName("service1");
		holon2Service1.setCost(0);
		holon2Service1.setUrl("http://holon2service1URL");
		holon2Service1.setReturnType(Types.BOOLEAN);
		ArrayList<ParameterModel> holon2Service1Parameters = new ArrayList<>();
		holon2Service1Parameters.add(new ParameterModel("speed", Types.STRING));
		holon2Service1Parameters.add(new ParameterModel("color", Types.STRING));
		holon2Service1.setParameters(holon2Service1Parameters);
		ArrayList<HolonServiceModel> holon2Services = new ArrayList<>();
		holon2Services.add(holon2Service1);
		holon2Model.setServices(holon2Services);
		Holon holon2 = new Holon(holon2Model);




	}

}
