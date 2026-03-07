package org.example.sbdbaspectscourse.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RoutingTestService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Transactional
    public boolean isReplicaNode() {
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject("SELECT pg_is_in_recovery()", Boolean.class));
    }

    @Transactional(readOnly = true)
    public boolean isReplicaNodeReadOnly() {
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject("SELECT pg_is_in_recovery()", Boolean.class));
    }
}