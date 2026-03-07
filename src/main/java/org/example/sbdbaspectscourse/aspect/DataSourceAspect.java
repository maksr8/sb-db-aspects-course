package org.example.sbdbaspectscourse.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.example.sbdbaspectscourse.config.DataSourceType;
import org.example.sbdbaspectscourse.config.DbContextHolder;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Aspect
@Component
@Order(0)
public class DataSourceAspect {

    @Around("@annotation(transactional)")
    public Object route(ProceedingJoinPoint joinPoint, Transactional transactional) throws Throwable {
        try {
            if (transactional.readOnly()) {
                DbContextHolder.setDbType(DataSourceType.REPLICA);
            } else {
                DbContextHolder.setDbType(DataSourceType.PRIMARY);
            }
            return joinPoint.proceed();
        } finally {
            DbContextHolder.clearDbType();
        }
    }
}