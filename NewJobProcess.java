package Drucker;

import co.paralleluniverse.fibers.SuspendExecution;
import desmoj.core.simulator.*;

public class NewJobProcess extends SimProcess {

	private PrinterModel printerModel;
	private JobType jobType;

	public NewJobProcess(Model owner, String name, boolean showInTrace) {
		super(owner, name, showInTrace);

		printerModel = (PrinterModel) owner;
	}

	@Override
	public void lifeCycle() throws SuspendExecution {
		
		while (true){

			// Prozess deaktivieren bis der naechste Job aktiviert werden soll
			hold(new TimeSpan(printerModel.getGenTime(jobType)));

			// neuen Job erzeugen
			JobProcess jobProcess = new JobProcess(printerModel, "Job", true, true);

			// Abarbeitungszeit setzen
			jobProcess.setJobExecutionTime(printerModel.getExecTime(jobType));

			// setzen des Job-Typs
			jobProcess.setType(jobType);
			jobProcess.setQueueingPriority(jobType.getPriority());
			// neuer Druckauftrag wurde erstellt
			// Job unmittelbar nach diesem Generator-Ereignis aktivieren
			jobProcess.activateAfter(this);

		}
		
	}

	public JobType getJobType() {
		return jobType;
	}

	public void setJobType(JobType jobType) {
		this.jobType = jobType;
	}
}
