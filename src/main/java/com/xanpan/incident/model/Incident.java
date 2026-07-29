package com.xanpan.incident.model;

import java.time.LocalDateTime;
import java.time.Clock;
import java.util.Objects;

public class Incident {

    private final String id;
    private String title;
    private String description;
    private Impact impact;
    private Urgency urgency;
    private Priority priority;
    private IncidentState state;
    private String category;
    private String solutionDescription;
    private boolean expedited;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private final Clock clock;
    private LocalDateTime closedAt;

    public Incident(String id, String title, String description, Impact impact, Urgency urgency, Priority priority, String category) {
        this(id, title, description, impact, urgency, priority, category, Clock.systemDefaultZone());
    }

    public Incident(
            String id,
            String title,
            String description,
            Impact impact,
            Urgency urgency,
            Priority priority,
            String category,
            Clock clock
    ) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.impact = impact;
        this.urgency = urgency;
        this.priority = priority;
        this.category = category;
        this.state = IncidentState.REGISTRADA;
        this.clock = Objects.requireNonNull(clock, "El reloj no puede ser nulo");
        LocalDateTime now = LocalDateTime.now(clock);
        this.createdAt = now;
        this.updatedAt = now;
    }

    private Incident(
            String id,
            String title,
            String description,
            Impact impact,
            Urgency urgency,
            Priority priority,
            IncidentState state,
            String category,
            String solutionDescription,
            boolean expedited,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            LocalDateTime closedAt
    ) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.impact = impact;
        this.urgency = urgency;
        this.priority = priority;
        this.state = state;
        this.category = category;
        this.solutionDescription = solutionDescription;
        this.expedited = expedited;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.closedAt = closedAt;
        this.clock = Clock.systemDefaultZone();
    }

    public static Incident restore(
            String id,
            String title,
            String description,
            Impact impact,
            Urgency urgency,
            Priority priority,
            IncidentState state,
            String category,
            String solutionDescription,
            boolean expedited,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            LocalDateTime closedAt
    ) {
        return new Incident(
                id, title, description, impact, urgency, priority, state, category,
                solutionDescription, expedited, createdAt, updatedAt, closedAt
        );
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
        touch();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
        touch();
    }

    public Impact getImpact() {
        return impact;
    }

    public void setImpact(Impact impact) {
        this.impact = impact;
        touch();
    }

    public Urgency getUrgency() {
        return urgency;
    }

    public void setUrgency(Urgency urgency) {
        this.urgency = urgency;
        touch();
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
        touch();
    }

    public IncidentState getState() {
        return state;
    }

    public void setState(IncidentState state) {
        this.state = state;
        LocalDateTime now = LocalDateTime.now(clock);
        this.updatedAt = now;
        if (state == IncidentState.FINALIZADA) {
            this.closedAt = now;
        }
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
        touch();
    }

    public String getSolutionDescription() {
        return solutionDescription;
    }

    public void setSolutionDescription(String solutionDescription) {
        this.solutionDescription = solutionDescription;
        touch();
    }

    public boolean isExpedited() {
        return expedited;
    }

    public void setExpedited(boolean expedited) {
        this.expedited = expedited;
        touch();
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public LocalDateTime getClosedAt() {
        return closedAt;
    }

    private void touch() {
        this.updatedAt = LocalDateTime.now(clock);
    }
}
