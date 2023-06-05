/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dionasys.holon.datamodel;


import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

import org.cloudbus.cloudsim.Log;
import org.cloudbus.iotnetsim.iot.nodes.holon.IoTNodeHolon;
import org.cloudbus.iotnetsim.iov.IoVNodeType;
import org.cloudbus.iotnetsim.iov.holon.IoVHolonAnnotations;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import dionasys.holon.Holon;
import dionasys.holon.HolonRegistry;

/**
 *
 * @author elhabbas
 */
public class Test3 {

    public static void main(String args[]) throws OWLOntologyCreationException{

        HolonDataModel holon1Model = new HolonDataModel();
        holon1Model.putData("name","holon1");
        holon1Model.putData("type",IoVNodeType.VEHICLE.toString());

        HolonServiceModel holon1Service1 = new HolonServiceModel();
        holon1Service1.setName("add");
        holon1Service1.setCost(0);
        holon1Service1.setUrl("add");
        holon1Service1.setReturnType(Types.STRING);
        //holon1Service1.setAnnotation(IoVHolonAnnotations.BOOK_TABLE);
        holon1Service1.setAnnotation(IoVHolonAnnotations.RESTAURANT_ORDER);
        ArrayList<ParameterModel> holon1Service1Parameters = new ArrayList<>();
        holon1Service1Parameters.add(new ParameterModel("x", Types.INTEGER));
        holon1Service1Parameters.add(new ParameterModel("y", Types.INTEGER));
        holon1Service1.setParameters(holon1Service1Parameters);
        
        HolonServiceModel holon1Service2 = new HolonServiceModel();
        holon1Service2.setName("isAvailable");
        holon1Service2.setCost(0);
        holon1Service2.setUrl("isAvailable");
        holon1Service2.setReturnType(Types.VOID);
        holon1Service2.setAnnotation(IoVHolonAnnotations.IS_FUEL_AVAILABLE);


        ArrayList<HolonServiceModel> holon1Services = new ArrayList<>();
        holon1Services.add(holon1Service1);
        holon1Services.add(holon1Service2);
        holon1Model.setServices(holon1Services);
        ArrayList<ParameterModel> holon1Service2Parameters = new ArrayList<>();
        holon1Service2.setParameters(holon1Service2Parameters);
        
        HolonRegistry registry = new HolonRegistry();
        
        TestClass holon1 = new TestClass(holon1Model);
        Object[] arguments = new Object[2];
        arguments[0] = "6";
        arguments[1] = 2;

        
        holon1.createOntology();
        System.out.println("*****************************************");
        System.out.println(OntologyParser.parse(holon1.getOntologyModel()));
        
        System.out.print(Test3.callService(null, holon1Service2, holon1));

    }
    
    
    public static Object callService(Object[] arguments, HolonServiceModel service, Holon node) {
		try {
			if (arguments != null && arguments.length < service.getParameters().size()) {
				System.out.println("Unable to cal service [" + service.getName()
						+ "], Insuffcient number of arguments");
				return null;
			}

			Class returnType = TypeMapper.mapToJavaTypes(service.getReturnType());

			Method method = null;
			Class[] methodArgumentTypes = null;
			if (service.getParameters().size() > 0) {
				methodArgumentTypes = new Class[service.getParameters().size()];
				int i = 0;
				for (ParameterModel par : service.getParameters()) {
					methodArgumentTypes[i] = TypeMapper.mapToJavaTypes(par.getDataType());
					arguments[i] = methodArgumentTypes[i].cast(arguments[i]);
					i++;
				}
				method = node.getClass().getDeclaredMethod(service.getName(), methodArgumentTypes);
			} else {
				method = node.getClass().getDeclaredMethod(service.getName());
			}

			if (service.getParameters().size() > 0) {
				return returnType.cast(method.invoke(node, arguments));
			} else {
				return returnType.cast(method.invoke(node));
			}
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException
				| SecurityException e) {
			e.printStackTrace();
			return null;
		}

	}

}