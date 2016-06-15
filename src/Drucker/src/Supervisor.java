package Drucker.src;

import co.paralleluniverse.fibers.SuspendExecution;
import desmoj.core.simulator.*;

public class Supervisor extends SimProcess {

    private PrinterModel printerModel;
    private PrinterProcess printerProcess;
    private ProcessQueue<JobProcess> jobProcessQueue;

    public Supervisor(Model owner, String name, boolean showInTrace) {

        super(owner, name, showInTrace);

        printerModel = (PrinterModel) owner;
    }

    @Override
    public void lifeCycle() throws SuspendExecution {

        while (true) {

            if (printerProcess.getCurrentProcess() == null)
                passivate();
            // Falls die Priorität des neuen Prozesses hoeher ist als der derzeit bearbeitete
            // und der derzeitige unterbrechbar ist, 
            // aktiviere den Drucker.
            else if ((printerProcess.getCurrentProcess().getType().getPriority() < jobProcessQueue.first().getType().getPriority())
            		&& (printerProcess.getCurrentProcess().isInterruptable() == true))
                printerProcess.activate();

            // Warten auf Reaktivierung durch den JobProcess
            passivate();
        }

    }

    public PrinterProcess getPrinterProcess() {
        return printerProcess;
    }

    public void setPrinterProcess(PrinterProcess printerProcess) {
        this.printerProcess = printerProcess;
    }

    public ProcessQueue<JobProcess> getJobProcessQueue() {
        return jobProcessQueue;
    }

    public void setJobProcessQueue(ProcessQueue<JobProcess> jobProcessQueue) {
        this.jobProcessQueue = jobProcessQueue;
    }
}
