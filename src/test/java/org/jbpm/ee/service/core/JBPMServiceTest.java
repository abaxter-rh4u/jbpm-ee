package org.jbpm.ee.service.core;

import static org.jbpm.ee.test.util.KJarUtil.createKieJar;
import static org.jbpm.ee.test.util.KJarUtil.getPom;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.kie.scanner.MavenRepository.getMavenRepository;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.compiler.kie.builder.impl.InternalKieModule;
import org.jboss.arquillian.transaction.api.annotation.TransactionMode;
import org.jboss.arquillian.transaction.api.annotation.Transactional;
import org.jbpm.ee.services.ProcessService;
import org.jbpm.ee.services.TaskService;
import org.jbpm.ee.support.KieReleaseId;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.api.KieServices;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.task.model.TaskSummary;
import org.kie.scanner.MavenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class JBPMServiceTest extends BaseJBPMServiceTest {
	private static final KieReleaseId KIE_RELEASE_ID = new KieReleaseId("com.redhat.demo", "testProj", "1.0-SNAPSHOT");
	private static final String PROCESS_IDENTIFIER = "testProj.testProcess";
	
	private static final Logger LOG = LoggerFactory.getLogger(JBPMServiceTest.class);
	
	public abstract TaskService getTaskService();
	public abstract ProcessService getProcessService();
	
	@BeforeClass
    public static void prepare() {
		KieServices ks = KieServices.Factory.get();
        List<String> processes = new ArrayList<String>();
        processes.add("src/test/resources/kjar/testProcess.bpmn2");
        InternalKieModule kjar = createKieJar(ks, KIE_RELEASE_ID.toReleaseIdImpl(), processes);
        File pom = new File("target/kmodule", "pom.xml");
        pom.getParentFile().mkdir();
        try {
            FileOutputStream fs = new FileOutputStream(pom);
            fs.write(getPom(KIE_RELEASE_ID).getBytes());
            fs.close();
        } catch (Exception e) {
            
        }
        MavenRepository repository = getMavenRepository();
        repository.deployArtifact(KIE_RELEASE_ID.toReleaseIdImpl(), kjar, pom);
    }
	
	@Test
	@Transactional(value=TransactionMode.DEFAULT)
	public void testSimpleProcess() throws Exception {
		TaskService taskService = getTaskService();
		ProcessService processService = getProcessService();
		
		Map<String, Object> processVariables = new HashMap<String, Object>();
		processVariables.put("processString", "Initial");
		
		List<TaskSummary> tasks = taskService.getTasksAssignedAsPotentialOwner("abaxter", "en-UK");
		
		int initialCount = tasks.size();
		LOG.info("Tasks: " + initialCount);
		ProcessInstance processInstance = processService.startProcess(KIE_RELEASE_ID, PROCESS_IDENTIFIER, processVariables);
		assertNotNull(processInstance);
        assertEquals(ProcessInstance.STATE_ACTIVE, processInstance.getState());
		
		tasks = taskService.getTasksAssignedAsPotentialOwner("abaxter", "en-UK");
		for(TaskSummary summary : tasks) {
			LOG.info("Task: " + summary.getId());
		}
		assertNotNull(tasks);
        assertEquals(initialCount + 1, tasks.size());
        
        long taskId = tasks.get(tasks.size()-1).getId();
        
        Map<String,Object> testResults = new HashMap<String,Object>();
        
        LOG.info("Claiming task: "+taskId);
        taskService.claim(taskId, "abaxter");
        
        LOG.info("Starting task: "+taskId);
        taskService.start(taskId, "abaxter");
        
        LOG.info("Completing task: "+taskId);
        taskService.complete(taskId, "abaxter", testResults);
        LOG.info("Completed task: "+taskId);
        // check the state of process instance
        
        
        tasks = taskService.getTasksAssignedAsPotentialOwner("abaxter", "en-UK");
		assertNotNull(tasks);
        assertEquals(initialCount, tasks.size());
        
        LOG.info("Looking up process instance: "+processInstance.getId());
        processInstance = processService.getProcessInstance(processInstance.getId());
        assertNull(processInstance);
	}
	

	@Test
	@Transactional(value=TransactionMode.DEFAULT)
	public void testMultiProcess() throws Exception {

		TaskService taskService = getTaskService();
		ProcessService processService = getProcessService();
		
		Map<String, Object> processVariables = new HashMap<String, Object>();
		processVariables.put("processString", "Initial");
		
		ProcessInstance instance1 = processService.startProcess(KIE_RELEASE_ID, PROCESS_IDENTIFIER);
		ProcessInstance instance2 = processService.startProcess(KIE_RELEASE_ID, PROCESS_IDENTIFIER);
		
		List<TaskSummary> tasks = taskService.getTasksAssignedAsPotentialOwner("abaxter", "en-UK");
		//claim and complete..
		for(TaskSummary summary : tasks) {
			taskService.claim(summary.getId(), "abaxter");
			taskService.start(summary.getId(), "abaxter");
			
			Map<String,Object> testResults = new HashMap<String,Object>();
			taskService.complete(summary.getId(), "abaxter", testResults);
		}
		
		//refresh the instances from the server.
		instance1 = processService.getProcessInstance(instance1.getId());
		instance2 = processService.getProcessInstance(instance2.getId());
		assertNull(instance1);
		assertNull(instance2);
		
	}
}
