package com.example.demo.modules.farmacia.bajas;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BajaRepository extends JpaRepository<BajaEntity, Long> {

    List<BajaEntity> findByLote_Id(Long loteId);

    List<BajaEntity> findAllByOrderByFechaHoraDesc();
}
