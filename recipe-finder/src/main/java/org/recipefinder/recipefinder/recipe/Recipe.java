package org.recipefinder.recipefinder.recipe;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.recipefinder.recipefinder.customer.Customer;

import java.util.Arrays;
import java.util.List;

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

    // add constraint that number must be 1 or greater

    @NotNull(message = "Number of servings is required")
    @Min(value = 1, message = "Number of servings must be 1 or greater")
    @Column(
            name = "num_servings",
            nullable = false,
            columnDefinition = "INTEGER"
    )
    @JsonProperty("num_servings")
    private Integer numServings;

    @NotBlank(message = "Ingredients are required")
    @Column(
            name = "ingredients",
            nullable = false,
            columnDefinition = "TEXT"
    )
    private String ingredients;

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

    public List<String> getIngredients() {
        return Arrays.asList(ingredients.split(","));
    }

    public void setIngredients(List<String> ingredients) {
        this.ingredients = String.join(",", ingredients).toLowerCase();
    }

}
