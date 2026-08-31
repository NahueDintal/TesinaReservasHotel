package models;

import java.math.BigDecimal;

public class Product {

    private int idProduct;
    private String name;
    private String description;
    private BigDecimal price;
    private boolean active;

    public Product() {
    }

    public Product(String name,
                   String description,
                   BigDecimal price,
                   boolean active) {

        this.name = name;
        this.description = description;
        this.price = price;
        this.active = active;
    }

    public int getIdProduct() {
        return idProduct;
    }

    public void setIdProduct(int idProduct) {
        this.idProduct = idProduct;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public String toString() {
        return name;
    }
}

