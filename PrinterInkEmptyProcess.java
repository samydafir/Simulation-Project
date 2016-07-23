package Drucker;

import co.paralleluniverse.fibers.SuspendExecution;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.SimProcess;

/**
 * Der PrinterInkEmptyProcess repräsentiert das Eintreten eines Wartungsereignisses.
 * @author Laurentiu Vlad
 * @author Thomas Samy Dafir
 * @author Dominik, Baumgartner
 */
public class PrinterInkEmptyProcess extends SimProcess {

    private PrinterModel printerModel;
    private PrinterProcess printerProcess;
    private double executionTime;

    public PrinterInkEmptyProcess(Model owner, String name, boolean showInTrace, double inkEmptyExecutionTime) {
        super(owner, name, showInTrace);
        this.executionTime = inkEmptyExecutionTime;
        printerModel = (PrinterModel) owner;
    }

    /**
     * Wird nach einem im PrinterModel festgelegten Zeitpunkt aktiviert. Wählt zufällig einen der beiden Drucker aus
     * und versetzt ihn in den Wartungszustand (alle ankommenden Job-Prozesse werden zum anderen Drucker umgeleitet).
     */
    @Override
    public void lifeCycle() throws SuspendExecution {

        int printerNumber = (int) (Math.random() * 2);

        switch (printerNumber) {
            case 0:
                printerProcess = printerModel.getFirstPrinter();
                break;
            case 1:
                printerProcess = printerModel.getSecondPrinter();
        }

        printerProcess.setIsInMaintainance(true);
        printerProcess.setInkEmptyProcess(this);

    }

    public double getExecutionTime() {
        return executionTime;
    }

    public void setExecutionTime(double executionTime) {
        this.executionTime = executionTime;
    }
}
