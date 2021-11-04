package com.gamingworld.app.gamingworld.tournament.domain.persitence;

import com.gamingworld.app.gamingworld.tournament.domain.model.entity.Tournament;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TournamentRepository extends JpaRepository<Tournament, Long> {

    @Query(value = "SELECT * FROM tournament t WHERE t.user_id = ?1", nativeQuery = true)
    public List<Tournament> findByUserId (Long tournamentId);



}
