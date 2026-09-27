package com.harding.meals.entity.receipt;

import com.harding.meals.entity.meal.ingredient.IngredientMetadata;
import jakarta.persistence.*;

@Entity
@Table(indexes = {
        @Index(name = "idx_grocery_item_receipt", columnList = "receipt_id"),
        @Index(name = "idx_grocery_item_metadata", columnList = "metadata_id")
})
public class GroceryItem {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    @JoinColumn(name = "receipt_id", nullable = false)
    private Receipt receipt;

    @Column(nullable = false)
    private String rawName;

    private Integer quantity;

    private Double unitPrice;

    private Double totalPrice;

    private String storageGroup;

    private boolean household;

    private String substitutedFrom;

    @ManyToOne
    @JoinColumn(name = "metadata_id")
    private IngredientMetadata metadata;

    public GroceryItem() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Receipt getReceipt() {
        return receipt;
    }

    public void setReceipt(Receipt receipt) {
        this.receipt = receipt;
    }

    public String getRawName() {
        return rawName;
    }

    public void setRawName(String rawName) {
        this.rawName = rawName;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(Double unitPrice) {
        this.unitPrice = unitPrice;
    }

    public Double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(Double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getStorageGroup() {
        return storageGroup;
    }

    public void setStorageGroup(String storageGroup) {
        this.storageGroup = storageGroup;
    }

    public boolean isHousehold() {
        return household;
    }

    public void setHousehold(boolean household) {
        this.household = household;
    }

    public String getSubstitutedFrom() {
        return substitutedFrom;
    }

    public void setSubstitutedFrom(String substitutedFrom) {
        this.substitutedFrom = substitutedFrom;
    }

    public IngredientMetadata getMetadata() {
        return metadata;
    }

    public void setMetadata(IngredientMetadata metadata) {
        this.metadata = metadata;
    }
}
