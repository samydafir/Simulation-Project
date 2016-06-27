package Drucker;

import co.paralleluniverse.fibers.SuspendExecution;
import desmoj.core.simulator.*;

public class PrinterProcess extends SimProcess{

	private PrinterModel printerModel;
	private boolean printerOccupied = false;
	private boolean printerInterrupted = false;
	private ProcessQueue<JobProcess> interruptedJobsQueue;
	private JobProcess currentProcess;

	public PrinterProcess(Model owner, String name, boolean showInTrace) {
		super(owner, name, showInTrace);

		printerModel = (PrinterModel) owner;
		interruptedJobsQueue = new ProcessQueue<JobProcess>(owner, "Unterbrochene Jobs - " + getName(), true, true);
	}

	@Override
	public void lifeCycle() throws SuspendExecution {
		String name;
		
		while (true){
			name = getName().substring(0, getName().length()-2);
			
			// Holen der Warteschlange welche zu diesem Drucker gehoert
			ProcessQueue correspondingQueue = printerModel.getCorrespondingQueue(name);

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
				TimeInstant t1 = printerModel.getExperiment().getSimClock().getTime();

				// Job wird abgearbeitet -> Drucker wird solange inaktiv gestellt
				hold(new TimeSpan(currentProcess.getJobExecutionTime()));

				// Endzeit der Abarbeitung des Jobs
				double endTime = printerModel.getExperiment().getSimClock().getTime().getTimeAsDouble();

				// Falls die Differenz zwischen Start- und Endzeit der Abarbeitung kleiner ist als
				// als die eigentliche Abarbeitungszeit des Jobs ist, bedeutet das, dass hier eine Unterbrechung
				// seitens des Supervisors vorgenommen wurde.
				double diff = Double.valueOf(String.valueOf(endTime - startingTime).substring(0, 7));
				double execTime = Double.valueOf(String.valueOf(currentProcess.getJobExecutionTime()).substring(0, 7));

				// && b != 0
				if (diff < execTime){

					// Prozess darf kein zweites mal unterbrochen werden
					currentProcess.setIsInterruptable(false);
					// Uebrige Druckzeit des Jobs ermitteln und setzen
					currentProcess.setJobExecutionTime(endTime - startingTime);
					// dann diesen Job in die Unterbrochenen-Queue dieses Druckers setzen
					interruptedJobsQueue.insert(currentProcess);
					
					printerInterrupted = true;
//					currentProcess.activate();


				}else{

					currentProcess.activate();
					//printerOccupied = false;
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

}
