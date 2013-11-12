package org.jbpm.ee.services.ejb.local;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;

import org.jbpm.ee.services.RuleService;
import org.jbpm.ee.services.ejb.remote.RuleServiceRemote;
import org.jbpm.ee.services.ejb.startup.KnowledgeManagerBean;

@Stateless
@LocalBean
public class RuleServiceBean implements RuleService, RuleServiceRemote {

	@EJB
	private KnowledgeManagerBean knowledgeManager;

	@Override
	public int fireAllRules(Long processInstanceId) {
		return knowledgeManager.getRuntimeEngineByProcessId(processInstanceId).getKieSession().fireAllRules();
	}

	@Override
	public int fireAllRules(Long processInstanceId, int max) {
		return knowledgeManager.getRuntimeEngineByProcessId(processInstanceId).getKieSession().fireAllRules(max);
	}

	@Override
	public void insert(Long processInstanceId, Object object) {
		knowledgeManager.getRuntimeEngineByProcessId(processInstanceId).getKieSession().insert(object);
	}
	
	

	
	
	
}
