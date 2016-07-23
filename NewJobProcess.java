package Drucker;

import co.paralleluniverse.fibers.SuspendExecution;
import desmoj.core.simulator.*;

/**
 * NewJobProcess �bernimmt die Erstellung neuer Jobs. Jedes NewJobProcess Objekt �bernimmt die Erstellung
 * eines Job Typs. Ein neues Job Objekt wir immer nach einer festgelegten Zwischenankunftszeit erstellt, mit
 * einer Ausf�hrungszeit versehen und aktiviert. Danach wartet der NewJobProcess auf den Ablauf der Zwischen-
 * Ankunftszeit bevor ein neues Objekt erstellt wird. Zwischenankunftszeiten und Ausf�hrungszeiten werden von
 * ContDistUniform- Objekten im Model zur�ckgegeben.
 * @author Laurentiu Vlad
 * @author Thomas Samy Dafir
 * @author Dominik, Baumgartner
 */
public class NewJobProcess extends SimProcess {

	private PrinterModel printerModel;
	private JobType jobType;

	public NewJobProcess(Model owner, String name, boolean showInTrace) {
		super(owner, name, showInTrace);

		printerModel = (PrinterModel) owner;
	}
	/**
	 * Wird vom PrinterModel aus aktiviert. L�uft w�hrend der gesamten Simulationsdauer und erstellt neue
	 * JobProcess-Objekte.
	 */
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
