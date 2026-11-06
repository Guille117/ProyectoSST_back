package com.example.demo.modules.clinica.estadoClinico;

import com.example.demo.modules.catalogo.entityBase;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "estados_clinicos")
@Data
@EqualsAndHashCode(callSuper = true)
public class estadoClinicoEntity extends entityBase {
}
