# Domain Events Documentation

## Übersicht

Dieses Paket enthält alle Domain Events des Content Management Systems. Domain Events repräsentieren wichtige
Geschäftsereignisse, die in der Domäne aufgetreten sind.

## Event-Typen

### User Events

#### UserCreatedEvent

- **Zweck**: Wird publiziert, wenn ein neuer Benutzer im System erstellt wird
- **Enthält**: `userId`, `userName`, `platformCount`
- **Verwendung**: Audit-Trail, Analytics, Integration mit anderen Systemen

### Content Events

#### ContentGeneratedEvent

- **Zweck**: Wird publiziert, wenn Inhalt durch AI-Generatoren erstellt wird
- **Enthält**: `contentId`, `generatorType`, `contentType`, `contentSize`, `generationParameters`
- **Verwendung**: Tracking von AI-Nutzung, Content-Analytics

### Distribution Events

#### DistributionScheduledEvent

- **Zweck**: Wird publiziert, wenn eine Content-Verteilung geplant wird
- **Enthält**: `distributionId`, `scheduledTime`, `platformNames`, `contentId`, `title`
- **Verwendung**: Scheduling, Workflow-Management

#### DistributionCompletedEvent vs. DistributionFailedEvent

**Wichtiger Unterschied:**

##### DistributionCompletedEvent

- **Zweck**: Nur für **erfolgreiche** Distributionen
- **Enthält**: `distributionId`, `platformName`, `contentId`, `executionDurationMs`, `platformPostId`
- **Wann**: Wenn `UploadStatus.FINISHED` erreicht wird
- **Semantik**: "Die Distribution wurde erfolgreich abgeschlossen"

##### DistributionFailedEvent

- **Zweck**: Nur für **fehlgeschlagene** Distributionen
- **Enthält**: `distributionId`, `platformName`, `reason`, `contentId`, `errorCode`, `retryable`, `attemptNumber`
- **Wann**: Wenn `UploadStatus.FAILED` erreicht wird
- **Semantik**: "Die Distribution ist fehlgeschlagen"

## Event-Struktur

Alle Domain Events implementieren das `DomainEvent` Interface:

```kotlin
interface DomainEvent {
    val eventId: UUID          // Eindeutige Event-ID
    val eventType: String      // Was ist passiert?
    val occurredAt: Instant    // Wann ist es passiert?
    val actorId: String        // Wer hat es getan?
    val createdBy: String      // Wer hat das Event erstellt?
    val version: Int           // Schema-Version
}
```

## Verwendung

### Event Publishing

```kotlin
// In Aggregate Roots
fun someBusinessOperation() {
    // ... business logic ...

    addDomainEvent(
        SomeEvent.create(
            // event parameters
        )
    )
}

// In Application Services
if (aggregate.hasUncommittedEvents()) {
    aggregate.getUncommittedEvents().forEach { event ->
        eventPublisher.publish(event)
    }
    aggregate.markEventsAsCommitted()
}
```

## Best Practices

1. **Immutabilität**: Events sind unveränderlich nach der Erstellung
2. **Vergangenheitsform**: Event-Namen beschreiben was passiert ist (z.B. "UserCreated", nicht "CreateUser")
3. **Geschäftssprache**: Events verwenden die Sprache der Domäne
4. **Vollständige Information**: Events enthalten alle relevanten Daten für Event Sourcing
5. **Versionierung**: Events haben Versionsnummern für Schema-Evolution 