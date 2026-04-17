package MiniProjet_Backend.Backend.DTO;

public class ScheduleConflictDTO {
    private String type;
    private Integer seanceId;
    private String message;

    public ScheduleConflictDTO() {
    }

    public ScheduleConflictDTO(String type, Integer seanceId, String message) {
        this.type = type;
        this.seanceId = seanceId;
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getSeanceId() {
        return seanceId;
    }

    public void setSeanceId(Integer seanceId) {
        this.seanceId = seanceId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
