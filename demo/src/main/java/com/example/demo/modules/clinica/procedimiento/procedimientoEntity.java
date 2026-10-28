package com.example.demo.modules.clinica.procedimiento;

import com.example.demo.modules.catalogo.entityBase;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "procedimientos")
@Data
@EqualsAndHashCode(callSuper = true)
public class procedimientoEntity extends entityBase {
}
