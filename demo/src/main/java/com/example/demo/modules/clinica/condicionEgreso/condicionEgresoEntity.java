package com.example.demo.modules.clinica.condicionEgreso;

import com.example.demo.modules.catalogo.entityBase;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "condiciones_egreso")
@Data
@EqualsAndHashCode(callSuper = true)
public class condicionEgresoEntity extends entityBase {
}
