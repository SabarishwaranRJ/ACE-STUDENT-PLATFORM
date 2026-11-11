package com.aceteamup.service;

import com.aceteamup.dto.event.EventRequest;
import com.aceteamup.dto.event.EventResponse;
import com.aceteamup.entity.Event;
import com.aceteamup.entity.Event.EventType;
import com.aceteamup.exception.ResourceNotFoundException;
import com.aceteamup.repository.EventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class EventService {

    private final EventRepository eventRepository;

    public EventService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    public List<EventResponse> getAllEvents() {
        return eventRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public EventResponse getEvent(Long id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found: " + id));
        return mapToResponse(event);
    }

    public List<EventResponse> getOpenEvents() {
        return eventRepository.findByRegistrationDeadlineAfter(LocalDate.now().minusDays(1)).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public EventResponse createEvent(EventRequest req) {
        EventType type = EventType.Other;
        if (req.getEventType() != null) {
            try {
                type = EventType.valueOf(req.getEventType());
            } catch (Exception ignored) {}
        }

        Event event = Event.builder()
                .eventName(req.getEventName())
                .description(req.getDescription())
                .eventDate(req.getEventDate())
                .registrationDeadline(req.getRegistrationDeadline())
                .requiredTeamSize(req.getRequiredTeamSize())
                .eventType(type)
                .eligibility(req.getEligibility())
                .build();

        Event saved = eventRepository.save(event);
        return mapToResponse(saved);
    }

    @Transactional
    public EventResponse updateEvent(Long id, EventRequest req) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found: " + id));

        if (req.getEventName() != null) event.setEventName(req.getEventName());
        if (req.getDescription() != null) event.setDescription(req.getDescription());
        if (req.getEventDate() != null) event.setEventDate(req.getEventDate());
        if (req.getRegistrationDeadline() != null) event.setRegistrationDeadline(req.getRegistrationDeadline());
        if (req.getRequiredTeamSize() != null) event.setRequiredTeamSize(req.getRequiredTeamSize());
        if (req.getEligibility() != null) event.setEligibility(req.getEligibility());
        if (req.getEventType() != null) {
            try {
                event.setEventType(EventType.valueOf(req.getEventType()));
            } catch (Exception ignored) {}
        }

        Event updated = eventRepository.save(event);
        return mapToResponse(updated);
    }

    @Transactional
    public void deleteEvent(Long id) {
        if (!eventRepository.existsById(id)) {
            throw new ResourceNotFoundException("Event not found: " + id);
        }
        eventRepository.deleteById(id);
    }

    public EventResponse mapToResponse(Event event) {
        return EventResponse.builder()
                .eventId(event.getEventId())
                .eventName(event.getEventName())
                .description(event.getDescription())
                .eventDate(event.getEventDate())
                .registrationDeadline(event.getRegistrationDeadline())
                .requiredTeamSize(event.getRequiredTeamSize())
                .eventType(event.getEventType() != null ? event.getEventType().name() : null)
                .eligibility(event.getEligibility())
                .createdAt(event.getCreatedAt())
                .build();
    }
}
