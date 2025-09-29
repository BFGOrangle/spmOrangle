package com.spmorangle.crm.fileupload.repository;

import java.util.List;
import com.spmorangle.crm.fileupload.model.File;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FileRepository extends JpaRepository<File, Long> {

    List<File> findByProjectId(Long projectId);

    List<File> findByTaskIdAndProjectId(Long taskId, Long projectId);
}
