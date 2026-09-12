package com.example.demo.modules.farmacia.viaAdmin;

import com.example.demo.modules.catalogo.entityBase;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "vias_admin")
@Data
@EqualsAndHashCode(callSuper = true)
public class viaAdminEntity extends entityBase {
}