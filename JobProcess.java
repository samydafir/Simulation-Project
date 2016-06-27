package Drucker;

import co.paralleluniverse.fibers.SuspendExecution;
import desmoj.core.simulator.*;

public class JobProcess extends SimProcess {

    private PrinterModel printerModel;
    private double jobExecutionTime;
    private JobType type;
    private boolean isInterruptable;

    public JobProcess(Model owner, String name, boolean showInTrace, boolean isInterruptable) {
        super(owner, name, showInTrace);
        this.isInterruptable = isInterruptable;
        printerModel = (PrinterModel) owner;
    }

    @Override
    public void lifeCycle() throws SuspendExecution {

        ProcessQueue smallestJobQueue = printerModel.getSmallestJobQueue();
    	boolean isInFirstQueue;
    	
    	if(!printerModel.getFirstPrinter().isPrinterOccupied()){
    		printerModel.firstPrinterQueue.insert(this);
            isInFirstQueue = true;
        } else if(!printerModel.getSecondPrinter().isPrinterOccupied()){
            printerModel.secondPrinterQueue.insert(this);
            isInFirstQueue = false;
        } else{
        // Einreihen des Jobs in die kleinste Drucker-Warteschlange
        	smallestJobQueue.insert(this);
        	isInFirstQueue = smallestJobQueue.getName().equals(NameConstants.WARTESCHLANGE_DRUCKER_1);
        }
        
        // Falls er sich in die erste eingereiht hat
        if (isInFirstQueue) {
            // Und falls der Drucker momentan beschaeftigt ist
            if (printerModel.getFirstPrinter().isPrinterOccupied()) {
                // wird der Supervisor dieses Druckers aktiviert (er uebernimmt alle weiteren Schritte)
                printerModel.getSupervisorPrinter1().activate();
            }else {
                // Anderenfalls wird der Drucker aktiviert. Dieser kuemmert sich darum, sich den Job aus
                // der WS zu holen
                PrinterProcess firstPrinter = printerModel.getFirstPrinter();
                firstPrinter.setPrinterOccupied(true);
                firstPrinter.activate();
            }

            // Ansonnsten befindet sich der Job in der WS des zweiten Druckers.
            // selber Ablauf wie oben.
        } else {

            if (printerModel.getSecondPrinter().isPrinterOccupied()) {
                printerModel.getSupervisorPrinter2().activate();
            }
            else {
                PrinterProcess secondPrinter = printerModel.getSecondPrinter();
                secondPrinter.setPrinterOccupied(true);
                secondPrinter.activate();
            }
        }

        // Warten in der WS oder auf die Abarbeitung durch den Drucker.
        passivate();
    }

    public double getJobExecutionTime() {
        return jobExecutionTime;
    }

    public void setJobExecutionTime(double jobExecutionTime) {
        this.jobExecutionTime = jobExecutionTime;
    }

    public JobType getType() {
        return type;
    }

    public void setType(JobType type) {
        this.type = type;
    }

	public boolean isInterruptable() {
		return isInterruptable;
	}

	public void setIsInterruptable(boolean isInterruptable) {
		this.isInterruptable = isInterruptable;
	}
    
    
}