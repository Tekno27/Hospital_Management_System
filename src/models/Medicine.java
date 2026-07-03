package models;

public class Medicine {

    private int id;
    private String name;
    private String category;
    private int stockQuantity;
    private double unitPrice;
    private int reorderLevel;
    private String expiryDate;

    public Medicine(int id, String name, String category, int stockQuantity,
                    double unitPrice, int reorderLevel, String expiryDate) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.stockQuantity = stockQuantity;
        this.unitPrice = unitPrice;
        this.reorderLevel = reorderLevel;
        this.expiryDate = expiryDate;
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

    public int getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(int stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(double unitPrice) {
        this.unitPrice = unitPrice;
    }

    public int getReorderLevel() {
        return reorderLevel;
    }

    public void setReorderLevel(int reorderLevel) {
        this.reorderLevel = reorderLevel;
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
    }

    public boolean isLowStock() {
        return stockQuantity <= reorderLevel;
    }

    @Override
    public String toString() {
        return name;
    }
}
