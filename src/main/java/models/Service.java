package models;

import java.math.BigDecimal;

public class Service {

    private int idService;
    private String name;
    private String description;
    private BigDecimal price;
    private boolean active;

    public Service() {
    }

    public Service(String name,
                   String description,
                   BigDecimal price,
                   boolean active) {

        this.name = name;
        this.description = description;
        this.price = price;
        this.active = active;
    }

    public int getIdService() {
        return idService;
    }

    public void setIdService(int idService) {
        this.idService = idService;
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

