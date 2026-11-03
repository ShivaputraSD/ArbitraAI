package Shivu.ArbitraAI.repository;

import Shivu.ArbitraAI.entity.ResearchReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResearchReportRepository extends JpaRepository<ResearchReport, Long> {
    List<ResearchReport> findAllByOrderByCreatedAtDesc();
}