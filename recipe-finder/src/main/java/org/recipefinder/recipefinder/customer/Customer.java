package org.recipefinder.recipefinder.customer;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.recipefinder.recipefinder.recipe.Recipe;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Entity(name = "Customer")
@Table(name = "customer")
@NoArgsConstructor
@Data
public class Customer implements UserDetails {

    public Customer(String email, String firstName, String lastName, String password, Role role) {
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.password = password;
        this.role = role;
    }

    @Id
    @SequenceGenerator(
            name = "customer_sequence",
            sequenceName = "customer_sequence",
            allocationSize = 1
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "customer_sequence"
    )
    @Column(
            name = "id",
            updatable = false
    )
    private Long id;

    @NotBlank(message = "Email is required")
    @Email(
            message = "Email should be valid",
            regexp = "^[\\w-\\.]+@([\\w-]+\\.)+[\\a-zA-Z]{2,4}$"
    )
    @Column(
            name = "email",
            nullable = false,
            unique = true,
            columnDefinition = "TEXT"
    )
    private String email;

    @NotBlank(message = "First name is required")
    @JsonProperty("first_name")
    @Pattern(
            regexp = "^[a-zA-Z- ]+$",
            message = "First name should be valid"
    )
    @Size(
            min = 2,
            max = 20,
            message = "First name should be between 2 and 20 characters"
    )
    @Column(
            name = "first_name",
            nullable = false,
            columnDefinition = "TEXT"
    )
    private String firstName;

    @NotBlank(message = "Last name is required")
    @JsonProperty("last_name")
    @Pattern(
            regexp = "^[a-zA-Z- ]+$",
            message = "Last name should be valid"
    )
    @Size(
            min = 2,
            max = 20,
            message = "Last name should be between 2 and 20 characters"
    )
    @Column(
            name = "last_name",
            nullable = false,
            columnDefinition = "TEXT"
    )
    private String lastName;

    @NotBlank(message = "Password is required")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(
            name = "password",
            nullable = false,
            columnDefinition = "TEXT"
    )
    private String password;

    @Transient
    @OneToMany(
            mappedBy = "customer",
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY,
            orphanRemoval = true
    )
    private List<Recipe> recipes;

    @Enumerated(EnumType.STRING)
    private Role role;

    public void addRecipe(Recipe recipe) {
        if (recipes == null) {
            recipes = new ArrayList<>();
        }

        if (!recipes.contains(recipe) && recipe != null) {
            recipes.add(recipe);
            recipe.setCustomer(this);
        }
    }

    public void removeRecipe(Recipe recipe) {
        if (recipes != null && recipes.contains(recipe)) {
            recipes.remove(recipe);
            recipe.setCustomer(null);
        }
    }

    // AUTH RELATED
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.name()));
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public String getPassword() {
        return password;
    }

}
