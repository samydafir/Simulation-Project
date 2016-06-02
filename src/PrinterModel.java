import desmoj.core.dist.ContDistUniform;
import desmoj.core.simulator.Model;

public class PrinterModel extends Model {
	
	private ContDistUniform StudJobGenTime;
	private ContDistUniform ProfJobGenTime;
	private ContDistUniform SysJobGenTime;
	private ContDistUniform StudJobExecTime;
	private ContDistUniform ProfJobExecTime;
	private ContDistUniform SysJobExecTime
	;
	

	public PrinterModel(Model arg0, String arg1, boolean arg2, boolean arg3) {
		super(arg0, arg1, arg2, arg3);
		// TODO Auto-generated constructor stub
	}

	public double getGenTime(int jobType){
		if(jobType == 1){
			return StudJobGenTime.sample();
		}else if(jobType == 2){
			return ProfJobGenTime.sample();
		} else if(jobType == 3){
			return SysJobGenTime.sample();
		}
		return 0;
	}
	
	public double getExecTime(int jobType){
		if(jobType == 1){
			return StudJobExecTime.sample();
		}else if(jobType == 2){
			return ProfJobExecTime.sample();
		} else if(jobType == 3){
			return SysJobExecTime.sample();
		}
		return 0;
	}
	
	
	
	
	
	@Override
	public String description() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void doInitialSchedules() {
		// TODO Auto-generated method stub

	}

	@Override
	public void init() {
		
	}

}
