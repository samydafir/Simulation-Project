package Drucker.src;

import co.paralleluniverse.fibers.SuspendExecution;
import desmoj.core.dist.ContDistUniform;
import desmoj.core.simulator.*;

public class PrinterProcess extends SimProcess{

	private PrinterModel printerModel;
	private boolean printerOccupied = false;
	private ProcessQueue<JobProcess> interruptedJobsQueue;
	private JobProcess currentProcess;

	public PrinterProcess(Model owner, String name, boolean showInTrace) {
		super(owner, name, showInTrace);

		printerModel = (PrinterModel) owner;
		interruptedJobsQueue = new ProcessQueue<JobProcess>(owner, "Unterbrochene Jobs - " + getName(), true, true);
	}

	@Override
	public void lifeCycle() throws SuspendExecution {

		while (true){
			// Holen der Warteschlange welche zu diesem Drucker gehoert
			ProcessQueue correspondingQueue = printerModel.getCorrespondingQueue(getName());

			// Falls die WS leer ist, wird das "besetzt"-Flag auf false gestellt und der Process auf passivate().
			if (correspondingQueue.isEmpty()){
				printerOccupied = false;
				passivate();
			}else {
				// Anderenfalls wird der erste Process aus der WS geholt und in der Variable gespeichert
				currentProcess = (JobProcess) correspondingQueue.first();
				// und dann aus der WS geloescht
				correspondingQueue.remove(currentProcess);

				// Festhalten der Startzeit des Abarbeitugsprozesses.
				// Dies ist noetig, im Falle einer Unterbrechung des Processes durch einen Job hoeherer Prioritaet
				double startingTime = printerModel.getExperiment().getSimClock().getTime().getTimeAsDouble();

				// Job wird abgearbeitet -> Drucker wird solange inaktiv gestellt
				hold(new TimeSpan(currentProcess.getJobExecutionTime()));

				// Endzeit der Abarbeitung des Jobs
				double endTime = printerModel.getExperiment().getSimClock().getTime().getTimeAsDouble();

				// Falls die Differenz zwischen Start- und Endzeit der Abarbeitung kleiner ist als
				// als die eigentliche Abarbeitungszeit des Jobs ist, bedeutet das, dass hier eine Unterbrechung
				// seitens des Supervisors vorgenommen wurde.
				if ((endTime - startingTime) < currentProcess.getJobExecutionTime()){
					// Uebrige Druckzeit des Jobs ermitteln und setzen
					currentProcess.setJobExecutionTime(endTime - startingTime);
					// dann diesen Job in die Unterbrochenen-Queue dieses Druckers setzen
					interruptedJobsQueue.insert(currentProcess);

				}


//				TimeOperations.diff()


				currentProcess.activateAfter(this);
			}

		}
		
	}

	public boolean isPrinterOccupied() {
		return printerOccupied;
	}

	public void setPrinterOccupied(boolean printerOccupied) {
		this.printerOccupied = printerOccupied;
	}

	public ProcessQueue<JobProcess> getInterruptedJobsQueue() {
		return interruptedJobsQueue;
	}

	public JobProcess getCurrentProcess() {
		return currentProcess;
	}

	public void setCurrentProcess(JobProcess currentProcess) {
		this.currentProcess = currentProcess;
	}
}
