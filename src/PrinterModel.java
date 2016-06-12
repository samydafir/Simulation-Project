package Drucker.src;

import desmoj.core.dist.ContDistUniform;
import desmoj.core.simulator.*;

public class PrinterModel extends Model {

	private ContDistUniform studJobGenTime;
	private ContDistUniform profJobGenTime;
	private ContDistUniform sysJobGenTime;

	private ContDistUniform studJobExecTime;
	private ContDistUniform profJobExecTime;
	private ContDistUniform sysJobExecTime;

	private PrinterProcess firstPrinter;
	private PrinterProcess secondPrinter;

	// Warteschlangen fuer Drucker
	protected ProcessQueue<JobProcess> firstPrinterQueue;
	protected ProcessQueue<JobProcess> secondPrinterQueue;

	protected Supervisor supervisorPrinter1;
	protected Supervisor supervisorPrinter2;


	// Warteschlange fuer Printer
//	protected ProcessQueue<PrinterProcess> freePrinterQueue;


	public PrinterModel(Model owner, String name, boolean showInReport, boolean showInTrace) {
		super(owner, name, showInReport, showInTrace);
	}

	public static void main(String[] args) {

		// Neues Experiment erzeugen
		Experiment printerExperiment = new Experiment("Drucker-Experiment (Prozess orientiert)");

		// neues Modell erzeugen
		// Par 1: null markiert main model, sonst Mastermodell angeben
		PrinterModel printerModel = new PrinterModel(null, "Drucker Modell", true, true);

		// Modell mit Experiment verbinden
		printerModel.connectToExperiment(printerExperiment);

		// Intervall fuer trace/debug
		printerExperiment.tracePeriod(new TimeInstant(0.0), new TimeInstant(60));
		printerExperiment.debugPeriod(new TimeInstant(0.0), new TimeInstant(60));

		// Ende der Simulation setzen
		// -> hier 4 Stunden (= 240 min)
		printerExperiment.stop(new TimeInstant(240));

		// Experiment zur Zeit 0.0 starten
		printerExperiment.start();

		// -> Simulation laeuft bis Abbruchkriterium erreicht ist
		// -> danach geht es hier weiter

		// Report generieren
		printerExperiment.report();

		// Ausgabekanaele schliessen, allfaellige threads beenden
		printerExperiment.finish();
	}


	public double getGenTime(JobType jobType){

		switch (jobType){
			case STUD_JOB:
				return studJobGenTime.sample();
			case PROFI_JOB:
				return profJobGenTime.sample();
			case SYSTEM_JOB:
				return sysJobGenTime.sample();
			default:
				return 0.0;
		}
	}

	public double getExecTime(JobType jobType){

		switch (jobType){
			case STUD_JOB:
				return studJobExecTime.sample();
			case PROFI_JOB:
				return profJobExecTime.sample();
			case SYSTEM_JOB:
				return sysJobExecTime.sample();
			default:
				return 0.0;
		}
	}

	@Override
	public String description() {
		// TODO Beschreibung ergaenzen!!!!!!!!
		return "Printer Modell (Prozess orientiert):";
	}

	@Override
	public void doInitialSchedules() {

		//
		NewJobProcess newStudJobProcess = new NewJobProcess(this, "Initiale StudJob Erstellung", true);
		newStudJobProcess.setJobType(JobType.STUD_JOB);

		NewJobProcess newProfiJobProcess = new NewJobProcess(this, "Initiale ProfiJob Erstellung", true);
		newProfiJobProcess.setJobType(JobType.PROFI_JOB);

		NewJobProcess newSystemJobProcess = new NewJobProcess(this, "Initiale SystemJob Erstellung", true);
		newStudJobProcess.setJobType(JobType.SYSTEM_JOB);

		// Drucker einrichten
		firstPrinter = new PrinterProcess(this, NameConstants.ERSTER_DRUCKER, true);
		secondPrinter = new PrinterProcess(this, NameConstants.ZWEITER_DRUCKER, true);

		// Initialisieren der Supervisor und setzen der dazugehoerigen Drucker und Warteschlangen
		supervisorPrinter1 = new Supervisor(this, NameConstants.SUPERVISOR_DRUCKER_1, true);
		supervisorPrinter1.setPrinterProcess(firstPrinter);
		supervisorPrinter1.setJobProcessQueue(firstPrinterQueue);
		supervisorPrinter2 = new Supervisor(this, NameConstants.SUPERVISOR_DRUCKER_2, true);
		supervisorPrinter2.setPrinterProcess(secondPrinter);
		supervisorPrinter2.setJobProcessQueue(secondPrinterQueue);

		// Supervisor Prozesse starten
		supervisorPrinter1.activate();
		supervisorPrinter2.activate();

		// Drucker Prozesse starten
		firstPrinter.activate(new TimeSpan(0.0));
		secondPrinter.activate(new TimeSpan(0.0));

		// Job Prozesse starten
		newStudJobProcess.activate(new TimeSpan(getGenTime(JobType.STUD_JOB)));
		newProfiJobProcess.activate(new TimeSpan(getGenTime(JobType.PROFI_JOB)));
		newSystemJobProcess.activate(new TimeSpan(getGenTime(JobType.SYSTEM_JOB)));

	}

	@Override
	public void init() {

		// Generatoren fuer Job-Ankuftszeiten
		studJobGenTime = new ContDistUniform(this, "StudJob-Ankunftsintervall", 6, 10, true, true);
		studJobGenTime.setNonNegative(true);
		profJobGenTime = new ContDistUniform(this, "ProfiJob-Ankunftsintervall", 10, 14, true, true);
		profJobGenTime.setNonNegative(true);
		sysJobGenTime = new ContDistUniform(this, "SystemJob-Ankunftsintervall", 15, 25, true, true);
		sysJobGenTime.setNonNegative(true);

		// Generatoren fuer Job-Abarbeitungszeiten
		studJobExecTime = new ContDistUniform(this, "StudJob-Abarbeitungszeit", 2, 6, true, true);
		studJobExecTime.setNonNegative(true);
		profJobExecTime= new ContDistUniform(this, "ProfiJob-Abarbeitungszeit", 2, 6, true, true);
		profJobExecTime.setNonNegative(true);
		sysJobExecTime = new ContDistUniform(this, "SystemJob-Abarbeitungszeit", 2, 10, true, true);
		sysJobExecTime.setNonNegative(true);

		// Warteschlangen fuer Drucker initialisieren
		firstPrinterQueue = new ProcessQueue<JobProcess>(this, NameConstants.WARTESCHLANGE_DRUCKER_1, true, true);
		secondPrinterQueue = new ProcessQueue<JobProcess>(this, NameConstants.WARTESCHLANGE_DRUCKER_2, true, true);

		// Warteschlange fuer freie Drucker initialisieren
//		freePrinterQueue = new ProcessQueue<PrinterProcess>(this, "Warteschlange fuer freie Drucker", true, true);

	}

	/**
	 * Berechnet die Gesamtbearbeitungszeit der Warteschlangen beider Drucker und zurueckgegeben die kuerzere.
	 * Falls beide gleich lang sind wird die des ersten Druckers zurueckgegeben.
	 * @return Die kleinste Warteschlange (kleinste Gesamtbearbeitungszeit)
	 */
	public ProcessQueue getSmallestJobQueue(){
		double firstQueueExecTime = 0.0;
		double secondQueueExecTime = 0.0;

		for(JobProcess jp1 : firstPrinterQueue)
			firstQueueExecTime += jp1.getJobExecutionTime();

		for (JobProcess jp2 : secondPrinterQueue)
			secondQueueExecTime += jp2.getJobExecutionTime();

		if (firstQueueExecTime <= secondQueueExecTime)
			return firstPrinterQueue;

		return secondPrinterQueue;
	}

	public PrinterProcess getSecondPrinter() {
		return secondPrinter;
	}

	public PrinterProcess getFirstPrinter() {
		return firstPrinter;
	}

	public Supervisor getSupervisorPrinter1() {
		return supervisorPrinter1;
	}

	public Supervisor getSupervisorPrinter2() {
		return supervisorPrinter2;
	}

	public ProcessQueue getCorrespondingQueue(String name){
		if (name.equals(NameConstants.ERSTER_DRUCKER))
			return firstPrinterQueue;
		return secondPrinterQueue;
	}
}
