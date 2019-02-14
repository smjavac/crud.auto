package my.vaadin;

import com.vaadin.ui.Grid;

public class Automobiles  {
    private int id;
    private String model;
    //private Integer year;
    private String body;

    public Automobiles() {
    }

    public Automobiles(int id, String model,  String csrbody) {
        this.id = id;
        this.model = model;
        //this.year = year;
        this.body = csrbody;
    }

    public int getId() {
        return id;
    }

    public String getModel() {
        return model;
    }

    /*public Integer getYear() {
        return year;
    }*/

    public String getBody() {
        return body;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setModel(String model) {
        this.model = model;
    }

    /*public void setYear(Integer year) {
        this.year = year;
    }
*/
    public void setBody(String body) {
        this.body = body;
    }
}
