package com.vinberts.shrinkly.service;

import com.vinberts.shrinkly.persistence.model.Abuse;
import com.vinberts.shrinkly.persistence.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 *
 */
public interface IAbuseService {

    Page<Abuse> findPaginated(User user, Pageable pageable);

    void saveNewAbuse(Abuse abuse);

}
