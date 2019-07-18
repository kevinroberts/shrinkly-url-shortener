package com.vinberts.shrinkly.service.impl;

import com.vinberts.shrinkly.persistence.dao.AbuseRepository;
import com.vinberts.shrinkly.persistence.model.Abuse;
import com.vinberts.shrinkly.persistence.model.User;
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
    public Page<Abuse> findPaginated(final User user, final Pageable pageable) {
        return null;
    }

    @Override
    public void saveNewAbuse(final Abuse abuse) {
        abuse.setDateAdded(LocalDateTime.now());
        abuseRepository.save(abuse);
    }
}
