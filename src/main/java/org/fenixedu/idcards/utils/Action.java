package org.fenixedu.idcards.utils;

public class Action {

    private String action;

    private String label;

    public Action() {

    }

    public Action(String action, String label) {
        this.action = action;
        this.label = label;

    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}
