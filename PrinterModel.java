package Drucker;

import desmoj.core.dist.ContDistUniform;
import desmoj.core.simulator.*;

/**
 * Das PrinterModel enthält alle Komponenten, die für die Simulation des Farbdrucker-Setup notwendig sind.
 * Hier werden unter anderem Queues, Drucker, Vereilungsfunktionen für Ankunfts- und Bearbeitungszeiten, sowie
 * Supervisor und Job-Generatoren definiert und mit den zugehörigen Werten initialisiert.
 * @author Laurentiu Vlad
 * @author Thomas Samy Dafir
 * @author Dominik, Baumgartner
 */
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
	

	public PrinterModel(Model owner, String name, boolean showInReport, boolean showInTrace) {
		super(owner, name, showInReport, showInTrace);
	}

	/**
	 * Neues Experiment mit allen notwendigen Parametern und einstellungen wird hoer definiert, gestartet und
	 * nach der angegebenen Simulationsdauer gestoppt.
	 * @param args cmd-args
	 */
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
		printerExperiment.stop(new TimeInstant(600));

		// Experiment zur Zeit 0.0 starten
		printerExperiment.start();

		// -> Simulation laeuft bis Abbruchkriterium erreicht ist
		// -> danach geht es hier weiter

		// Report generieren
		printerExperiment.report();

		// Ausgabekanaele schliessen, allfaellige threads beenden
		printerExperiment.finish();
	}


	/**
	 * Liefert für den angegebenen JobTyp die nächste Zwischenankunftszeit zurück
	 * @param jobType Job-Typ
	 * @return Zugehörige Zwischenankunftszeit
	 */
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
	
	/**
	 * Liefert für den angegebenen JobTyp die nächste Bearbeitungszeit zurück
	 * @param jobType Job-Typ
	 * @return Zugehörige Bearbeitungszeit
	 */
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
		String description = "PrinterModel:\n"+
							 "simuliert einen Aufbau aus 2 Farbdruckern mit jeweils einer Queue fuer neu ankommende"+
							 "Auftraege, sowie einer fuer unterbrochene Auftraege. Neue Jobs werden erstellt und reihen"+
							 "sich in die kuerzere, zu einem Drucker gehoerende Warteschlange ein. Die Drucker arbeiten"+
							 "dann die Auftraege einen nach dem anderen ab. sollte ein Auftrag mit hoeherer Prioritaet"+
							 "als der derzeit bearbeitete Auftrag in eine Queue gelangen, muss der autuelle Auftrag unterbrochen"+
							 "werden (in die Unterbrochenen-Queue verschoben werden). Die unterbrochenen Auftraege werden"+
							 "dann sobals als moeglich fortgesetzt";
		return description;
	}

	/**
	 * Hier werden essentielle Komponenten wie Job-Generatoren, Drucker und der Wartungsprozess
	 * erstellt, mit den entsprechenden Werten initialisiert und aktiviert oder für die Aktivierung vporbereitet.
	 */
	@Override
	public void doInitialSchedules() {

		//
		NewJobProcess newStudJobProcess = new NewJobProcess(this, "Initiale StudJob Erstellung", true);
		newStudJobProcess.setJobType(JobType.STUD_JOB);

		NewJobProcess newProfiJobProcess = new NewJobProcess(this, "Initiale ProfiJob Erstellung", true);
		newProfiJobProcess.setJobType(JobType.PROFI_JOB);

		NewJobProcess newSystemJobProcess = new NewJobProcess(this, "Initiale SystemJob Erstellung", true);
		newSystemJobProcess.setJobType(JobType.SYSTEM_JOB);
		
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

		// Drucker Prozesse starten
		firstPrinter.activate(new TimeSpan(0.0));
		secondPrinter.activate(new TimeSpan(0.0));

		// Job Prozesse starten
		newStudJobProcess.activate(new TimeSpan(0.0));
		newProfiJobProcess.activate(new TimeSpan(0.0));
		newSystemJobProcess.activate(new TimeSpan(0.0));

		// InkEmpty Prozess initialisieren und starten
		PrinterInkEmptyProcess printerInkEmptyProcess = new PrinterInkEmptyProcess(this,
				"Initiale InkEmptyProcess Erstellung", true, 100);
		printerInkEmptyProcess.activate(new TimeSpan(100));

		PrinterInkEmptyProcess printerInkEmptyProcess2 = new PrinterInkEmptyProcess(this,
				"Initiale InkEmptyProcess Erstellung", true, 100);
		printerInkEmptyProcess2.activate(new TimeSpan(300));

	}
	
	/**
	 * Hier werden die Generatoren für die Ankunfts- und Ausführungszeit, sowie die Drucker-
	 * Queues erstellt. die Generatoren werden mit den angegebenen Werten initialisiert. Es
	 * wurde eine gleichverteilung gewählt.
	 */
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
	}

	/**
	 * Berechnet die Gesamtbearbeitungszeit der Warteschlangen beider Drucker und zurueckgegeben die kuerzere.
	 * Falls beide gleich lang sind wird die des ersten Druckers zurueckgegeben.
	 * @return Die kleinste Warteschlange (kleinste Gesamtbearbeitungszeit)
	 */
	public ProcessQueue<JobProcess> getSmallestJobQueue(){
		double firstQueueExecTime = 0.0;
		double secondQueueExecTime = 0.0;

		for(JobProcess jp1 : firstPrinterQueue)
			firstQueueExecTime += jp1.getJobExecutionTime();

		for (JobProcess jp2 : secondPrinterQueue)
			secondQueueExecTime += jp2.getJobExecutionTime();
		
		if (firstQueueExecTime <= secondQueueExecTime){
			return firstPrinterQueue;
		}
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

	/**
	 * Gibt die zum angegebenen Drucker gehörende Queue zurück
	 * @param name Drucker
	 * @return zugehörige Prozess-Queue
	 */
	public ProcessQueue<JobProcess> getCorrespondingQueue(String name){
		if (name.equals(NameConstants.ERSTER_DRUCKER))
			return firstPrinterQueue;
		return secondPrinterQueue;
	}

	/**
	 * Gibt zu einem Drucker die Queue des jeweils anderen Druckers zurück
	 * @param name Drucker
	 * @return Queue des anderen Druckers
	 */
	public ProcessQueue<JobProcess> getOtherPrinterQueue(String name){
		if (name.equals(NameConstants.ERSTER_DRUCKER))
			return secondPrinterQueue;
		return firstPrinterQueue;
	}

	/**
	 * Gibt zu einem Drucker den anderen Drucker zurück
	 * @param name Drucker
	 * @return Anderer Drucker
	 */
	public PrinterProcess getOtherPrinterProcess(String name){
		if (name.equals(NameConstants.ERSTER_DRUCKER))
			return secondPrinter;
		return firstPrinter;
	}
}
