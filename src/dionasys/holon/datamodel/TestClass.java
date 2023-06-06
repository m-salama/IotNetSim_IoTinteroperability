package dionasys.holon.datamodel;

import dionasys.holon.Holon;

public class TestClass extends Holon{
	
	public TestClass(HolonDataModel model) {
		super(model);
	}
	public void isAvailable() {
	}
	
	public Integer add(Integer x, Integer y) {
		return x+y;
	}

}
