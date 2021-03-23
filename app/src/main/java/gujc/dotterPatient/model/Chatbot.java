package gujc.dotterPatient.model;

public class Chatbot {
    private String name;
    private String current;

    public Chatbot(String name, String current) {
        this.name = name;
        this.current = current;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCurrent() {
        return current;
    }

    public void setCurrent(String current) {
        this.current = current;
    }
}
