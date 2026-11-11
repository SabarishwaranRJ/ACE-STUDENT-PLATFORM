package com.aceteamup.repository;

import com.aceteamup.entity.Event;
import com.aceteamup.entity.Event.EventType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Spring Data JPA repository for {@link Event}.
 */
@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    /** Filter events by type (Hackathon, Datathon, etc.). */
    List<Event> findByEventType(EventType eventType);

    /**
     * Return all events whose registration deadline is strictly after the
     * given date — i.e., events still open for registration.
     */
    List<Event> findByRegistrationDeadlineAfter(LocalDate date);

    /**
     * Return upcoming events of a specific type with open registration.
     * Useful for the event discovery feed.
     */
    List<Event> findByEventTypeAndRegistrationDeadlineAfterOrderByEventDateAsc(
            EventType eventType, LocalDate date);
}

