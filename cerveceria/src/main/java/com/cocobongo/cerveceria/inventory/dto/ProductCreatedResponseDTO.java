package com.cocobongo.cerveceria.inventory.dto;

import com.cocobongo.cerveceria.branches.dto.BranchResponseDTO;

public class ProductCreatedResponseDTO {

    private ProductResponseDTO product;
    private BranchResponseDTO branch;
    private InventoryResponseDTO inventory;

    public ProductCreatedResponseDTO(ProductResponseDTO product, BranchResponseDTO branch, InventoryResponseDTO inventory) {
        this.product = product;
        this.branch = branch;
        this.inventory = inventory;
    }

    public ProductResponseDTO getProduct() { return product; }
    public void setProduct(ProductResponseDTO product) { this.product = product; }

    public BranchResponseDTO getBranch() { return branch; }
    public void setBranch(BranchResponseDTO branch) { this.branch = branch; }

    public InventoryResponseDTO getInventory() { return inventory; }
    public void setInventory(InventoryResponseDTO inventory) { this.inventory = inventory; }
}