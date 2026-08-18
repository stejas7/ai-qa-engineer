package com.aiqa.scorpion;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface ScorpionMissionRepository extends JpaRepository<ScorpionMission, UUID> {}
