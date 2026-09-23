package com.example.demo.modules.pacientes.area;

import com.example.demo.modules.catalogo.entityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "areas")
@Data
@EqualsAndHashCode(callSuper = true)
public class areaEntity extends entityBase {

    @Column(nullable = false, length = 255)
    private String descripcion;
}