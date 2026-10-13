package com.example.backend.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.example.backend.Model.Iscrizione;

@Repository
public interface IscrizioneRepository extends JpaRepository<Iscrizione, Integer> {

    @Query("SELECT COUNT(i) FROM Iscrizione i WHERE i.torneo.id = :torneoId")
    int countByTorneoId(@Param("torneoId") Integer torneoId);

    @Query("SELECT CASE WHEN COUNT(i) > 0 THEN true ELSE false END FROM Iscrizione i WHERE i.torneo.id = :torneoId AND i.utente.id = :utenteId")
    boolean existsByTorneoIdAndUtenteId(@Param("torneoId") int torneoId, @Param("utenteId") int utenteId);

    @Transactional
    @Modifying
    @Query("DELETE FROM Iscrizione i WHERE i.torneo.id = :torneoId AND i.utente.id = :utenteId")
    int deleteByTorneoIdAndUtenteId(@Param("torneoId") int torneoId, @Param("utenteId") int utenteId);

    @Query("SELECT i.torneo.id FROM Iscrizione i WHERE i.utente.id = :utenteId")
    List<Integer> findTorneoIdsByUtenteId(@Param("utenteId") Integer utenteId);

    @Query("SELECT i FROM Iscrizione i WHERE i.utente.id = :utenteId")
    List<Iscrizione> findByUtenteId(@Param("utenteId") int utenteId);

    @Query("SELECT i.torneo.id, COUNT(i) FROM Iscrizione i GROUP BY i.torneo.id")
    List<Object[]> countIscrittiPerTorneo();
    
	@Transactional
	@Modifying
	@Query("DELETE FROM Iscrizione i WHERE i.torneo.id = :torneoId")
	void deleteByTorneoId(@Param("torneoId") int torneoId);
	
	
	
	
}