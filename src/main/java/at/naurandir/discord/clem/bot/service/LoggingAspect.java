package at.naurandir.discord.clem.bot.service;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

/**
 *
 * @author Naurandir
 */
@Slf4j
@Aspect
@Component
public class LoggingAspect {

    @Around("@annotation(org.springframework.scheduling.annotation.Scheduled)")
    public Object logScheduledExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        StopWatch watch = new StopWatch();
        watch.start();

        Object proceed = joinPoint.proceed();

        watch.stop();

        if (watch.getTotalTimeMillis() > 5_000) {
            log.warn("logScheduledExecutionTime: took [{}]ms to execute task [{}]", watch.getTotalTimeMillis(), joinPoint);
        }

        return proceed;
    }
}
