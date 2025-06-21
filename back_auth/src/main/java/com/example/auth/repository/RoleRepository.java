package com.example.auth.repository;

import com.example.auth.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Интерфейс репозитория для работы с ролями пользователей.
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, UUID> {

    /**
     * Поиск роли по её названию.
     *
     * @param name название роли
     * @return роль, если она существует, иначе Optional.empty()
     */
    Optional<Role> findByName(String name);

}
