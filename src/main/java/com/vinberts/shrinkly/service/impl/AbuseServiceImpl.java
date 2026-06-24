package com.vinberts.shrinkly.service.impl;

import com.vinberts.shrinkly.persistence.dao.AbuseRepository;
import com.vinberts.shrinkly.persistence.model.Abuse;
import com.vinberts.shrinkly.service.IAbuseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 *
 */
@Service
@Transactional
@Slf4j
public class AbuseServiceImpl implements IAbuseService {

    @Autowired
    AbuseRepository abuseRepository;

    @Override
    public Page<Abuse> findAllPaginated(final Pageable pageable) {
        return abuseRepository.findAll(pageable);
    }

    @Override
    public Page<Abuse> findUnaddressedPaginated(final Pageable pageable) {
        return abuseRepository.queryAllByAddressedFalse(pageable);
    }

    @Override
    public Abuse getAbuseById(final Long id) {
        return abuseRepository.getAbuseById(id);
    }

    @Override
    public void removeAbuseById(final Long id) {
        abuseRepository.deleteAbuseById(id);
    }

    @Override
    public void markAbuseAddressed(final Long id) {
        Abuse abuse = abuseRepository.getAbuseById(id);
        abuse.setAddressed(true);
        abuseRepository.save(abuse);
    }

    @Override
    public void saveNewAbuse(final Abuse abuse) {
        abuse.setDateAdded(LocalDateTime.now());
        abuse.setAddressed(false);
        abuseRepository.save(abuse);
    }
}
