package com.digital5.repository;

import com.digital5.entity.OneTimesEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OneTimeKeyRepository extends JpaRepository<OneTimesEntity, String> {

}
