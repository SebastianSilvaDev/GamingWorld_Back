package com.gamingworld.app.gamingworld.externalapi.domain.persistence;

import com.gamingworld.app.gamingworld.externalapi.domain.model.entity.ExternalAPI;
import com.gamingworld.app.gamingworld.tournament.domain.model.entity.Participant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExternalAPIRepository extends JpaRepository<ExternalAPI, Long> {
    @Query(value = "SELECT ea FROM ExternalAPI ea WHERE ea.name = :apiName ORDER BY ea.id DESC")
    public List<ExternalAPI> findByExternalAPIName (@Param("apiName") String apiName);
}
