package models;

public class LabTest {

    private int id;
    private String name;
    private String category;
    private double price;
    private String normalRange;

    public LabTest(int id, String name, String category, double price, String normalRange) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.price = price;
        this.normalRange = normalRange;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getNormalRange() {
        return normalRange;
    }

    public void setNormalRange(String normalRange) {
        this.normalRange = normalRange;
    }

    @Override
    public String toString() {
        return name;
    }
}
