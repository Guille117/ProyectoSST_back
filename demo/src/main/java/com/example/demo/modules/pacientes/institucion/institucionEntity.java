package com.example.demo.modules.pacientes.institucion;

import com.example.demo.modules.catalogo.entityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "instituciones")
@Data
@EqualsAndHashCode(callSuper = true)
public class institucionEntity extends entityBase {

    @Column(length = 255)
    private String direccion;

    @Column(length = 30)
    private String telefono;
}