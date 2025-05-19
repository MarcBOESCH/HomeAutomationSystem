package at.fhv.sysarch.lab2.homeautomation.order;

public class Product {
    private final String name;
    private final double price;
    private final double weight;

    public Product(String name, double price, double weight) {
        this.name = name;
        this.price = price;
        this.weight = weight;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }
    public double getWeight() {
        return weight;
    }

}
