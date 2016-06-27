package Drucker;

import co.paralleluniverse.fibers.SuspendExecution;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.SimProcess;

/**
 * Created by laurentiu on 27.06.2016.
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
