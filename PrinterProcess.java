package Drucker;

import co.paralleluniverse.fibers.SuspendExecution;
import desmoj.core.simulator.*;

/**
 * Jeder PrinterProcess repräsentiert einen Drucker. Dieser läuft während der gesamten Simulationsdauer.
 * Der PrinterProcess hat die Aufgabe, den Prozess mit der jeweils höchsten Priorität aus der Queue zu holen und zu bearbeiten.
 * Dabei kann der jeweilige Bearbeitungsvorgang von einem neu eintreffenden Prozess höherer Priorität unterborchen werden.
 * Auch Wartungsereignisse werden hier berücksichtigt. Tritt eine Wartungsereignis ein, wird der jeweilige Drucker auf hold
 * gesetzt, bis die Wartung beendet ist. 
 * @author Laurentiu Vlad
 * @author Thomas Samy Dafir
 * @author Dominik, Baumgartner
 */
public class PrinterProcess extends SimProcess{

	private PrinterModel printerModel;
	private boolean printerOccupied = false;
	private boolean printerInterrupted = false;
	private ProcessQueue<JobProcess> interruptedJobsQueue;
	private JobProcess currentProcess;
	private boolean isInMaintainance = false;
	private PrinterInkEmptyProcess inkEmptyProcess;

	public PrinterProcess(Model owner, String name, boolean showInTrace) {
		super(owner, name, showInTrace);

		printerModel = (PrinterModel) owner;
		interruptedJobsQueue = new ProcessQueue<JobProcess>(owner, "Unterbrochene Jobs - " + getName(), true, true);
	}
	
	/**
	 * Wird sofort bei Beginn der Simulation vom Model aus gestartet. Verwaltet Abarbeitung, Unterbrehung und Wartung. Wird ein Job
	 * bearbeitet, wird der PrinterProcess auf hold gesetzt, bis dieser Job abgearbeitet ist. Kommt es jedoch zu einer Unterbrechung,
	 * wird dies aufgrund der noch nicht abgelaufenen Bearbeitungszeit des Jobs festgestellt und der job wird mit der Restzeit in die 
	 * Unterbrochenen-Queue verschoben. Danach wird der Job höchster Priorität aus der Prozess-Queue geholt und bearbeitet.
	 * Kommt es zur Wartung, werden alle Jobs in die Queue des anderen Druckers verschoben und der Drucker selbst aud hold gesetzt.
	 */
	@Override
	public void lifeCycle() throws SuspendExecution {
		String name;
		
		while (true){
			// Falls der Drucker in Wartung ist (z.B. Toner Wechsel), wird die Zeit der Wartung abgehalten.
			if (isInMaintainance){
				hold(new TimeSpan(inkEmptyProcess.getExecutionTime()));
				isInMaintainance = false;
			}

			name = getName().substring(0, getName().length()-2);
			
			// Holen der Warteschlange welche zu diesem Drucker gehoert
			ProcessQueue<JobProcess> correspondingQueue = printerModel.getCorrespondingQueue(name);

			// Falls die WS leer ist, wird das "besetzt"-Flag auf false gestellt und der Process auf passivate().
			if (correspondingQueue.isEmpty() && interruptedJobsQueue.isEmpty()){
				printerOccupied = false;
				passivate();
			}else {
				// Zuerst ein check auf die unterbrochenen WS da diese Vorrang bei der Bearbeitung hat.
				if(!interruptedJobsQueue.isEmpty() && printerInterrupted == false){
					currentProcess = (JobProcess) interruptedJobsQueue.first();
					interruptedJobsQueue.remove(currentProcess);

				}else{
					// Anderenfalls wird der erste Process aus der WS geholt und in der Variable gespeichert
					currentProcess = (JobProcess) correspondingQueue.first();
					// und dann aus der WS geloescht
					correspondingQueue.remove(currentProcess);

				}
				
				printerOccupied = true;

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
				double diff = Double.valueOf(String.valueOf(endTime - startingTime).substring(0, 7)) + 0.0001;
				double execTime = Double.valueOf(String.valueOf(currentProcess.getJobExecutionTime()).substring(0, 7));

				if (isInMaintainance){

					JobProcess jp = new JobProcess(printerModel, currentProcess.getName(), true, true);
					jp.setJobExecutionTime(printerModel.getExecTime(currentProcess.getType()));
					jp.setType(currentProcess.getType());
					jp.setQueueingPriority(currentProcess.getType().getPriority());
					jp.activate();


					ProcessQueue<JobProcess> otherPrinterQueue = printerModel.getOtherPrinterQueue(name);
					ProcessQueue<JobProcess> otherInterruptedJobsQueue = printerModel.getOtherPrinterProcess(name).getInterruptedJobsQueue();

					for (Object process : correspondingQueue){
						otherPrinterQueue.insert((JobProcess) process);
						correspondingQueue.remove((JobProcess) process);
					}

					for (Object process : interruptedJobsQueue){
						otherInterruptedJobsQueue.insert((JobProcess) process);
						interruptedJobsQueue.remove((JobProcess) process);
					}

					printerOccupied = false;
					printerInterrupted = false;

					currentProcess = null;

					continue;
				}

				if (diff < execTime){

					// Prozess darf kein zweites mal unterbrochen werden
					currentProcess.setIsInterruptable(false);
					// Uebrige Druckzeit des Jobs ermitteln und setzen
					currentProcess.setJobExecutionTime(endTime - startingTime);
					// dann diesen Job in die Unterbrochenen-Queue dieses Druckers setzen
					interruptedJobsQueue.insert(currentProcess);
					
					printerInterrupted = true;

				}else{
					currentProcess.activate();
					printerInterrupted = false;
				}

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

	public PrinterInkEmptyProcess getInkEmptyProcess() {
		return inkEmptyProcess;
	}

	public void setInkEmptyProcess(PrinterInkEmptyProcess inkEmptyProcess) {
		this.inkEmptyProcess = inkEmptyProcess;
	}

	public boolean isInMaintainance() {
		return isInMaintainance;
	}

	public void setIsInMaintainance(boolean isInMaintainance) {
		this.isInMaintainance = isInMaintainance;
	}


}
