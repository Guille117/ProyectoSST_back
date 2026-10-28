package com.example.demo.modules.pacientes.parentesco;

import com.example.demo.modules.catalogo.entityBase;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "parentescos")
@EqualsAndHashCode(callSuper = true)
public class parentescoEntity extends entityBase {
}