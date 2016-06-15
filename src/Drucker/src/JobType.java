package Drucker.src;

/**
 *
 */
public enum JobType {

    STUD_JOB(1),
    PROFI_JOB(2),
    SYSTEM_JOB(3);

    private final int priority;

    JobType(int priority) {
        this.priority = priority;
    }

    public int getPriority(){
        return priority;
    }
}
