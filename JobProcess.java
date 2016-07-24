package Drucker;

import co.paralleluniverse.fibers.SuspendExecution;
import desmoj.core.simulator.*;

/**
 * JobProcess repraesentiert Printer Jobs. Um welchen Typ von Job es sich handelt, wird mit den Eigenschaften
 * des jeweiligen Objekts festgelegt. Jeder Job hat die Aufgabe, die Printer-Queues und Printer zu analysieren
 * und sich so einzureihen, dass eine moeglichst kurze Wartezeit entsteht -> jeder Job reiht sich also in die
 * Queue mit der niedrigsten Gesamtbearbeitungszeit ein.
 * @author Laurentiu Vlad
 * @author Thomas Samy Dafir
 * @author Dominik, Baumgartner
 */
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

    /**
     * Wird aufgerufen, sobald ein neuer Prozess in NewJbProcess erstellt wurde. Hier werden 2 verschiedene
     * Verhaltensweisen unterschieden:
     * 1. Ist eine Printer-Queue leer, reiht sich dieser JobProcess in diese Queue ein und setzt den dazugehoerigen
     *    Printer sofort auf besetzt.
     * 2. enthalten beide Queues mindestens einen Job, analysiert der JobProcess alle Queues und reiht sich in die mit
     * 	  der kuerzesten Gesamtbearbeitungszeit ein.
     * Das herausnehmen der Prozesse aus den Queues uebernehmen die Printer selbst. Der Job wird passiviert, sobald er
     * sich eingereiht hat.
     */
    @Override
    public void lifeCycle() throws SuspendExecution {

        ProcessQueue<JobProcess> smallestJobQueue = printerModel.getSmallestJobQueue();
        boolean isInFirstQueue;

        boolean firstPrinterInMaintainance = printerModel.getFirstPrinter().isInMaintainance();
        boolean secondPrinterInMaintainance = printerModel.getSecondPrinter().isInMaintainance();

        /*
         * Hier wird abgefragt, ob sich ein Drucker im Wartungszustand befindet. Ist dies der Fall,
         * reiht sich der aktuelle Job sofort in die Queue des anderen Druckers ein. 
         */
        if (firstPrinterInMaintainance){
            printerModel.secondPrinterQueue.insert(this);
            isInFirstQueue = false;
        } else if (secondPrinterInMaintainance){
            printerModel.firstPrinterQueue.insert(this);
            isInFirstQueue = true;
        }else {
        	
        	/* Hier wird ueberprueft, ob ein Drucker frei ist. Ist dies der Fall, reiht sich der Job sofort
        	 * in dessen Queue ein.
        	 */
            if (!printerModel.getFirstPrinter().isPrinterOccupied()) {
                printerModel.firstPrinterQueue.insert(this);
                isInFirstQueue = true;
            } else if (!printerModel.getSecondPrinter().isPrinterOccupied()) {
                printerModel.secondPrinterQueue.insert(this);
                isInFirstQueue = false;
            } else {
                // Einreihen des Jobs in die kleinste Drucker-Warteschlange
                smallestJobQueue.insert(this);
                isInFirstQueue = smallestJobQueue.getName().equals(NameConstants.WARTESCHLANGE_DRUCKER_1);
            }
        }

        // Falls er sich in die erste eingereiht hat
        if (isInFirstQueue) {
            // Und falls der Drucker momentan beschaeftigt ist
            if (printerModel.getFirstPrinter().isPrinterOccupied()) {
                // wird der Supervisor dieses Druckers aktiviert (er uebernimmt alle weiteren Schritte)
                printerModel.getSupervisorPrinter1().activate();
            } else {
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
            } else {
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
