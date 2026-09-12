package com.example.demo.modules.farmacia.unidadMedida;

import com.example.demo.modules.catalogo.entityBase;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "unidad_medidas")
@Data
@EqualsAndHashCode(callSuper = true)
public class unidadMedidaEntity extends entityBase {
}