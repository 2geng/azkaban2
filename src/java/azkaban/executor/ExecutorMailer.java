package azkaban.executor;

import java.util.ArrayList;
import java.util.List;

import javax.mail.MessagingException;

import org.apache.log4j.Logger;

import azkaban.executor.ExecutionOptions.FailureAction;
import azkaban.executor.ExecutorManager.Alerter;
import azkaban.sla.SlaOption;
import azkaban.utils.AbstractMailer;
import azkaban.utils.EmailMessage;
import azkaban.utils.Props;
import azkaban.utils.Utils;

public class ExecutorMailer extends AbstractMailer implements Alerter {
	private static Logger logger = Logger.getLogger(ExecutorMailer.class);
	
	private boolean testMode = false;
	
	public ExecutorMailer(Props props) {
		super(props);

		testMode = props.getBoolean("test.mode", false);
	}
	
	@SuppressWarnings("unchecked")
	private void sendSlaAlertEmail(SlaOption slaOption, String slaMessage, String subject) {
		//String subject = "Sla Alert";
		String body = slaMessage;
		List<String> emailList = (List<String>) slaOption.getInfo().get(SlaOption.INFO_EMAIL_LIST);
		if (emailList != null && !emailList.isEmpty()) {
			EmailMessage message = super.createEmailMessage(
					subject, 
					"text/html", 
					emailList);
			
			message.setBody(body);
			
			if (!testMode) {
				try {
					message.sendEmail();
				} catch (MessagingException e) {
					logger.error("Email message send failed" , e);
				}
			}
		}
	}
	
	public void sendFirstErrorMessage(ExecutableFlow flow) {
		ExecutionOptions option = flow.getExecutionOptions();
		List<String> emailList = option.getDisabledJobs();
		int execId = flow.getExecutionId();
		
		if (emailList != null && !emailList.isEmpty()) {
			EmailMessage message = super.createEmailMessage(
					"Flow '" + flow.getFlowId() + "' has failed on " + getAzkabanName(), 
					"text/html", 
					emailList);
			
			message.println("<h2 style=\"color:#FF0000\"> Execution '" + flow.getExecutionId() + "' of flow '" + flow.getFlowId() + "' has encountered a failure on " + getAzkabanName() + "</h2>");
			
			if (option.getFailureAction() == FailureAction.CANCEL_ALL) {
				message.println("This flow is set to cancel all currently running jobs.");
			}
			else if (option.getFailureAction() == FailureAction.FINISH_ALL_POSSIBLE){
				message.println("This flow is set to complete all jobs that aren't blocked by the failure.");
			}
			else {
				message.println("This flow is set to complete all currently running jobs before stopping.");
			}
			
			message.println("<table>");
			message.println("<tr><td>Start Time</td><td>" + flow.getStartTime() +"</td></tr>");
			message.println("<tr><td>End Time</td><td>" + flow.getEndTime() +"</td></tr>");
			message.println("<tr><td>Duration</td><td>" + Utils.formatDuration(flow.getStartTime(), flow.getEndTime()) +"</td></tr>");
			message.println("</table>");
			message.println("");
			String executionUrl = super.getReferenceURL() + "executor?" + "execid=" + execId;
			message.println("<a href='\"" + executionUrl + "\">" + flow.getFlowId() + " Execution Link</a>");
			
			message.println("");
			message.println("<h3>Reason</h3>");
			List<String> failedJobs = findFailedJobs(flow);
			message.println("<ul>");
			for (String jobId : failedJobs) {
				message.println("<li><a href=\"" + executionUrl + "&job=" + jobId + "\">Failed job '" + jobId + "' Link</a></li>" );
			}
			
			message.println("</ul>");
			
			if (!testMode) {
				try {
					message.sendEmail();
				} catch (MessagingException e) {
					logger.error("Email message send failed" , e);
				}
			}
		}
	}
	
	public void sendErrorEmail(ExecutableFlow flow, String ... extraReasons) {
		ExecutionOptions option = flow.getExecutionOptions();
		
		List<String> emailList = option.getFailureEmails();
		int execId = flow.getExecutionId();
		
		if (emailList != null && !emailList.isEmpty()) {
			EmailMessage message = super.createEmailMessage(
					"Flow '" + flow.getFlowId() + "' has failed on " + getAzkabanName(), 
					"text/html", 
					emailList);
			
			message.println("<h2 style=\"color:#FF0000\"> Execution '" + execId + "' of flow '" + flow.getFlowId() + "' has failed on " + getAzkabanName() + "</h2>");
			message.println("<table>");
			message.println("<tr><td>Start Time</td><td>" + flow.getStartTime() +"</td></tr>");
			message.println("<tr><td>End Time</td><td>" + flow.getEndTime() +"</td></tr>");
			message.println("<tr><td>Duration</td><td>" + Utils.formatDuration(flow.getStartTime(), flow.getEndTime()) +"</td></tr>");
			message.println("</table>");
			message.println("");
			
			String executionUrl = super.getReferenceURL() + "executor?" + "execid=" + execId;
			message.println("<a href='\"" + executionUrl + "\">" + flow.getFlowId() + " Execution Link</a>");
			
			message.println("");
			message.println("<h3>Reason</h3>");
			List<String> failedJobs = findFailedJobs(flow);
			message.println("<ul>");
			for (String jobId : failedJobs) {
				message.println("<li><a href=\"" + executionUrl + "&job=" + jobId + "\">Failed job '" + jobId + "' Link</a></li>" );
			}
			for (String reasons: extraReasons) {
				message.println("<li>" + reasons + "</li>");
			}
			
			message.println("</ul>");
			
			if (!testMode) {
				try {
					message.sendEmail();
				} catch (MessagingException e) {
					logger.error("Email message send failed" , e);
				}
			}
		}
	}

	public void sendSuccessEmail(ExecutableFlow flow) {
		ExecutionOptions option = flow.getExecutionOptions();
		List<String> emailList = option.getSuccessEmails();

		int execId = flow.getExecutionId();
		
		if (emailList != null && !emailList.isEmpty()) {
			EmailMessage message = super.createEmailMessage(
					"Flow '" + flow.getFlowId() + "' has succeeded on " + getAzkabanName(), 
					"text/html", 
					emailList);
			
			message.println("<h2> Execution '" + flow.getExecutionId() + "' of flow '" + flow.getFlowId() + "' has succeeded on " + getAzkabanName() + "</h2>");
			message.println("<table>");
			message.println("<tr><td>Start Time</td><td>" + flow.getStartTime() +"</td></tr>");
			message.println("<tr><td>End Time</td><td>" + flow.getEndTime() +"</td></tr>");
			message.println("<tr><td>Duration</td><td>" + Utils.formatDuration(flow.getStartTime(), flow.getEndTime()) +"</td></tr>");
			message.println("</table>");
			message.println("");
			String executionUrl = super.getReferenceURL() + "executor?" + "execid=" + execId;
			message.println("<a href=\"" + executionUrl + "\">" + flow.getFlowId() + " Execution Link</a>");
			
			if (!testMode) {
				try {
					message.sendEmail();
				} catch (MessagingException e) {
					logger.error("Email message send failed" , e);
				}
			}
		}
	}
	
	private List<String> findFailedJobs(ExecutableFlow flow) {
		ArrayList<String> failedJobs = new ArrayList<String>();
		for (ExecutableNode node: flow.getExecutableNodes()) {
			if (node.getStatus() == Status.FAILED) {
				failedJobs.add(node.getJobId());
			}
		}
		
		return failedJobs;
	}

	@Override
	public void alertOnSuccess(ExecutableFlow exflow) throws Exception {
		sendSuccessEmail(exflow);
	}
	
	@Override
	public void alertOnError(ExecutableFlow exflow, String ... extraReasons) throws Exception {
		sendErrorEmail(exflow, extraReasons);
	}
	
	@Override
	public void alertOnFirstError(ExecutableFlow exflow) throws Exception {
		sendFirstErrorMessage(exflow);
	}

	@Override
	public void alertOnSla(SlaOption slaOption, String slaMessage, String subject) throws Exception {
		sendSlaAlertEmail(slaOption, slaMessage, subject);		
	}
}