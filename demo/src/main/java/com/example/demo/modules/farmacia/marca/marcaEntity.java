package com.example.demo.modules.farmacia.marca;

import com.example.demo.modules.catalogo.entityBase;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "marcas")
@Data
@EqualsAndHashCode(callSuper = true)
public class marcaEntity extends entityBase {
}