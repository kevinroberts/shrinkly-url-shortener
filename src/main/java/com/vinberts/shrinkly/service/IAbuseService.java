package com.vinberts.shrinkly.service;

import com.vinberts.shrinkly.persistence.model.Abuse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 *
 */
public interface IAbuseService {

    Page<Abuse> findAllPaginated(Pageable pageable);

    Page<Abuse> findUnaddressedPaginated(Pageable pageable);

    Abuse getAbuseById(Long id);

    void saveNewAbuse(Abuse abuse);

    void markAbuseAddressed(Long id);

    void removeAbuseById(Long id);

}
