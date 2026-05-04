package dev.guillemdiaz.herald.repository;

import dev.guillemdiaz.herald.entity.MessageLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface MessageLogRepository extends JpaRepository<MessageLog, Long> {

    List<MessageLog> findAllByTenantId(Long tenantId);

    Optional<MessageLog> findByIdAndTenantId(Long id, Long tenantId);
}