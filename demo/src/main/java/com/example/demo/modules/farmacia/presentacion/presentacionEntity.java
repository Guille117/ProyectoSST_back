package com.example.demo.modules.farmacia.presentacion;

import com.example.demo.modules.catalogo.entityBase;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "presentaciones")
@Data
@EqualsAndHashCode(callSuper = true)
public class presentacionEntity extends entityBase {
}