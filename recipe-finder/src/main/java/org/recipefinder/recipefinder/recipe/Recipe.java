package org.recipefinder.recipefinder.recipe;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.recipefinder.recipefinder.customer.Customer;
import java.util.Map;

@Entity(name = "Recipe")
@Table(name = "recipe")
@Data
@NoArgsConstructor
public class Recipe {

    @Id
    @SequenceGenerator(
            name = "recipe_sequence",
            sequenceName = "recipe_sequence",
            allocationSize = 1
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "recipe_sequence"
    )
    @Column(
            name = "id",
            updatable = false
    )
    private Long id;

    @NotBlank(message = "Description is required")
    @Column(
            name = "description",
            nullable = false,
            columnDefinition = "TEXT"
    )
    private String description;

    @NotNull(message = "Is vegan is required")
    @Column(
            name = "is_vegan",
            nullable = false,
            columnDefinition = "BOOLEAN"
    )
    @JsonProperty("is_vegan")
    private Boolean isVegan;

    @Column(
            name = "num_servings",
            nullable = false,
            columnDefinition = "INTEGER"
    )
    @JsonProperty("num_servings")
    private Integer numServings;

    @ElementCollection(targetClass = String.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "ingredients", joinColumns = @JoinColumn(name = "recipe_id"))
    @Column(
            name = "quantity",
            nullable = false
    )
    private Map<String, String> ingredients;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(
            name = "customer_id",
            nullable = false,
            referencedColumnName = "id",
            foreignKey = @ForeignKey(
                    name = "customer_account_fk"
            )
    )
    private Customer customer;

}
