package com.example.demo.modules.usuarios.puesto;

import com.example.demo.modules.catalogo.entityBase;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "puestos")
@Data // Genera automáticamente getters, setters, toString, equals y hashCode
@EqualsAndHashCode(callSuper = true) // Incluye los campos de la clase padre (entityBase) en equals y hashCode
public class puestoEntity extends entityBase {
    
}
