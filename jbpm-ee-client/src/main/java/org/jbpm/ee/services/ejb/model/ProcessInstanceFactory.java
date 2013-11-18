package org.jbpm.ee.services.ejb.model;

import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.runtime.process.WorkItem;

public class ProcessInstanceFactory {

	private ProcessInstanceFactory() {
		// seal
	}
	
	public static ProcessInstance convert(ProcessInstance instance) {
		if(instance == null) {
			return null;
		}
		return new org.jbpm.ee.services.ejb.model.process.ProcessInstance(instance);
	}
	
	public static WorkItem convert(WorkItem instance) {
		if(instance == null) {
			return null;
		}
		return new org.jbpm.ee.services.ejb.model.process.WorkItem(instance);
	}
}