package dionasys.holon.datamodel;

import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLLiteral;

public class TypeMapper {

	public static Class mapToJavaTypes(int type) {
		try {
			switch (type) {
			case Types.VOID:
				return Class.forName("java.lang.Void");
			case Types.INTEGER:
				return Class.forName("java.lang.Integer");
			case Types.DOUBLE:
				return Class.forName("java.lang.Double");
			case Types.BOOLEAN:
				return Class.forName("java.lang.Boolean");
			case Types.STRING:
				return Class.forName("java.lang.String");
			default:
				return null;
			}
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}

	}
	
	public static OWLDatatype mapToOWLTypes(int type, OWLDataFactory dataFactory) {
		switch (type) {
			case Types.INTEGER:
				return dataFactory.getIntegerOWLDatatype();
			case Types.DOUBLE:
				return dataFactory.getDoubleOWLDatatype();
			case Types.BOOLEAN:
				return dataFactory.getBooleanOWLDatatype();
			case Types.STRING:
				return dataFactory.getStringOWLDatatype();
			default:
				return null;
			}

	}
	
	public static int mapFromOWLTypes(OWLLiteral literal) {
		String type = literal.getDatatype().toStringID().split("#")[1].toLowerCase();
		switch (type) {
		case "integer":
			return Types.INTEGER;
		case "double":
			return Types.DOUBLE;
		case "boolean":
			return Types.BOOLEAN;
		case "string":
			if(literal.getLiteral().equalsIgnoreCase("void")) {
				return Types.VOID;
			}
			return Types.STRING;
		default:
			return -1;
		}
	}
}
