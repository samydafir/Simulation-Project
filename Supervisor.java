package Drucker;

import co.paralleluniverse.fibers.SuspendExecution;
import desmoj.core.simulator.*;

/**
 * Der Supervisor analysiert bei jedem eintreffen eines neuen Job-Prozesses in einer Queue dessen Priorit�t und
 * vergleicht sie mit der des aktuellen Jobs im Drucker.Bei h�herer Priorit�t des neuen Jobs wird der Drucker
 * unterbrochen.
 * @author Laurentiu Vlad
 * @author Thomas Samy Dafir
 * @author Dominik, Baumgartner
 */
public class Supervisor extends SimProcess {

    private PrinterProcess printerProcess;
    private ProcessQueue<JobProcess> jobProcessQueue;

    public Supervisor(Model owner, String name, boolean showInTrace) {

        super(owner, name, showInTrace);
    }

    /**
     * wird vom JobProcess aktiviert, sobald sich dieser in eine Queue einordnet. Hier wird dann die Priorit�t
     * des aktuellen Prozesses im Drucker mit der des neuen Jobs vergleichen. Ist die der neuen JobProcess-Objekts
     * h�her, wird der Drucker aktiviert (dieser f�hrt dann die eigentliche Unterbrechung durch).
     */
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
            // Falls die Priorit�t des neuen Prozesses hoeher ist als der derzeit bearbeitete
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
