package org.springframework.samples.petclinic.util;

import org.json.simple.JSONObject;
import org.springframework.core.style.ToStringStyler;

public class JsonToStringStyler implements ToStringStyler {

	JSONObject object = null;
	
	@Override
	public void styleStart(StringBuilder buffer, Object obj) {
		new JSONObject();
		// TODO Auto-generated method stub
		
	}

	@Override
	public void styleEnd(StringBuilder buffer, Object obj) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void styleField(StringBuilder buffer, String fieldName, Object value) {
		object.put(fieldName, value.toString());
		// TODO Auto-generated method stub
		
	}

	@Override
	public void styleValue(StringBuilder buffer, Object value) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void styleFieldSeparator(StringBuilder buffer) {
		// TODO Auto-generated method stub
		
	}
	
	

}
