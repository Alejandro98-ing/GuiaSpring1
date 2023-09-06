package dev.lopez.bootcamp1.security.dto;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class ProductoDto {
    
    @NotBlank
    private String nombre;
    @Min(0)
    private Float precio;

    public ProductoDto(@NotBlank String nombre, @Min(0) Float precio){
        this.nombre = nombre;
        this.precio = precio;
    }
}
