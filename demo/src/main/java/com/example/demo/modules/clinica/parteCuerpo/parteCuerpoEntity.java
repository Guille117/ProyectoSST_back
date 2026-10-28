package com.example.demo.modules.clinica.parteCuerpo;

import com.example.demo.modules.catalogo.entityBase;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "partes_cuerpo")
@Data
@EqualsAndHashCode(callSuper = true)
public class parteCuerpoEntity extends entityBase {
}
