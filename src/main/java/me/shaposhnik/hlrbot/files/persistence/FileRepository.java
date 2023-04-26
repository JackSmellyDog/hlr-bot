package me.shaposhnik.hlrbot.files.persistence;

import java.util.Optional;
import javax.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FileRepository extends JpaRepository<FileEntity, String> {

  Optional<FileEntity> findByIdAndDeletedFalse(String id);

  @Modifying
  @Query("update FileEntity f set f.deleted = true where f.id = :id")
  @Transactional
  void markDeleted(@Param("id") String id);
}
