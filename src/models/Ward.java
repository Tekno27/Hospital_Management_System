package models;

public class Ward {

    private int id;
    private String name;
    private String department;
    private int bedCapacity;
    private String floor;
    private String notes;
    private int occupied;
    private int nurseCount;

    public Ward(int id, String name, String department, int bedCapacity, String floor, String notes,
                int occupied, int nurseCount) {
        this.id = id;
        this.name = name;
        this.department = department;
        this.bedCapacity = bedCapacity;
        this.floor = floor;
        this.notes = notes;
        this.occupied = occupied;
        this.nurseCount = nurseCount;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getDepartment() { return department; }
    public int getBedCapacity() { return bedCapacity; }
    public String getFloor() { return floor; }
    public String getNotes() { return notes; }
    public int getOccupied() { return occupied; }
    public int getNurseCount() { return nurseCount; }

    public int getAvailable() {
        return Math.max(0, bedCapacity - occupied);
    }

    public double getOccupancyRate() {
        return bedCapacity == 0 ? 0 : (occupied * 100.0 / bedCapacity);
    }
}
