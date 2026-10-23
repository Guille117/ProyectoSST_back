package com.example.demo.modules.pacientes.enfermedad;

import com.example.demo.modules.catalogo.entityBase;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "enfermedades")
@Data
@EqualsAndHashCode(callSuper = true)
public class enfermedadEntity extends entityBase {
}
