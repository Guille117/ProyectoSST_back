package com.example.demo.modules.farmacia.motivoBaja;

import com.example.demo.modules.catalogo.entityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "motivo_bajas")
@Data
@EqualsAndHashCode(callSuper = true)
public class motivoBajaEntity extends entityBase {
    @Column(length = 500)
    private String descripcion;
}
