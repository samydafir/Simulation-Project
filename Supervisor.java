package Drucker;

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

            JobProcess currentProcess = printerProcess.getCurrentProcess();
            JobProcess newJobProcess = jobProcessQueue.first();
        	int process1 = currentProcess.getType().getPriority();
            int process2 = newJobProcess.getType().getPriority();
            boolean processHasHigherPriority = process1 < process2;
            boolean isProcessInterruptible = printerProcess.getCurrentProcess().isInterruptable() == true;

            if (printerProcess.getCurrentProcess() == null)
                passivate();
            // Falls die Priorität des neuen Prozesses hoeher ist als der derzeit bearbeitete
            // und der derzeitige unterbrechbar ist, 
            // aktiviere den Drucker.
            else if (processHasHigherPriority && isProcessInterruptible){
                printerProcess.reActivate(new TimeSpan(0.0));
            }

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
