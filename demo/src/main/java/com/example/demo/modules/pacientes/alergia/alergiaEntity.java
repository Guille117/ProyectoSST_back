package com.example.demo.modules.pacientes.alergia;

import com.example.demo.modules.catalogo.entityBase;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "alergias")
@Data
@EqualsAndHashCode(callSuper = true)
public class alergiaEntity extends entityBase {
}
