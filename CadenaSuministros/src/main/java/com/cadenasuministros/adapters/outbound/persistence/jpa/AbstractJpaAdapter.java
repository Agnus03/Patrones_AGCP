package com.cadenasuministros.adapters.outbound.persistence.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public abstract class AbstractJpaAdapter<D, E> {

    protected final JpaRepository<E, UUID> repo;

    protected AbstractJpaAdapter(JpaRepository<E, UUID> repo) {
        this.repo = repo;
    }

    protected abstract E toEntity(D domain);

    protected abstract D toDomain(E entity);

    public D save(D domain) {
        E entity = toEntity(domain);
        E saved = repo.save(entity);
        return toDomain(saved);
    }

    public Optional<D> findById(UUID id) {
        return repo.findById(id).map(this::toDomain);
    }

    public List<D> findAll() {
        return repo.findAll().stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

}
