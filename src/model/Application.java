package model;

public class Application {
    private int id;
    private String company;
    private String position;
    private String applicationDate;
    private String status;
    private String notes;

    public Application(int id, String company, String position, String applicationDate, String status, String notes) {
        this.id = id;
        this.company = company;
        this.position = position;
        this.applicationDate = applicationDate;
        this.status = status;
        this.notes = notes;
    }

    public int getId() {
        return id;
    }
    public String getCompany() {
        return company;
    }
    public String getPosition() {
        return position;
    }
    public String getApplicationDate() {
        return applicationDate;
    }
    public String getStatus() {
        return status;
    }
    public String getNotes() {
        return notes;
    }
    public void setCompany(String company) {
        this.company = company;
    }
    public void setPosition(String position) {
        this.position = position;
    }
    public void setApplicationDate(String applicationDate) {
        this.applicationDate = applicationDate;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    public void setNotes(String notes) {
        this.notes = notes;
    }

    @Override
    public String toString() {
        return "Application{" +
                "id=" + id +
                ", company='" + company + '\'' +
                ", position='" + position + '\'' +
                ", applicationDate=" + applicationDate +
                ", status='" + status + '\'' +
                ", notes='" + notes + '\'' +
                '}';    
    }
}
