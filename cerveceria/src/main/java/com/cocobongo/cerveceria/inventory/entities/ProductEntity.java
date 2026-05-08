package com.cocobongo.cerveceria.inventory.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import lombok.Data;

@Data
@Entity
public class ProductEntity {
          @Id
          @GeneratedValue(strategy = GenerationType.IDENTITY)
          private Integer id_product;
          private String name;
          private String description;
          private ProductType type;
          private int cost;
          private int price;
          private boolean is_active;
          
          @OneToMany(fetch = FetchType.LAZY)
          @JoinColumn(
                    name = "id_provider",
                    insertable =  false,
                    updatable = false,
                    foreignKey = @ForeignKey(name = "fk_product_provider")
          )
          private ProviderEntity provider;
}
