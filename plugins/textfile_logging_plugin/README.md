# TextFile Logging Plugin

Dieses Plugin stellt eine dateibasierte Implementierung des `DomainEventPublisher` zur Verfügung, die alle Domain Events
in Textdateien protokolliert.

## Features

- **Session-basiertes Logging**: Jede Anwendungssitzung erstellt eine neue Log-Datei mit Zeitstempel
- **Strukturierte Log-Einträge**: Jedes Event wird mit vollständigen Metadaten protokolliert
- **Automatische Verzeichniserstellung**: Log-Verzeichnisse werden automatisch erstellt
- **Graceful Shutdown**: Session wird ordnungsgemäß beendet mit Footer-Informationen

## Verwendung

### Einfache Verwendung

```kotlin
val fileLogger = TextFileEventLogger()
val event = UserCreatedEvent.create(userId, userName, platformCount)
fileLogger.publish(event)
fileLogger.closeSession() // Am Ende der Anwendung
```

### Als einziger Event Publisher

```kotlin
val eventPublisher: DomainEventPublisher = TextFileEventLogger()

// Shutdown Hook für ordnungsgemäße Beendigung
Runtime.getRuntime().addShutdownHook(Thread {
    (eventPublisher as TextFileEventLogger).closeSession()
})
```

### Konfiguration

```kotlin
// Standard-Verzeichnis: "logs/domain-events"
val fileLogger = TextFileEventLogger()

// Benutzerdefiniertes Verzeichnis
val fileLogger = TextFileEventLogger("custom/log/path")
```

## Log-Datei Format

### Session Header

```
****************************************************************************************************
DOMAIN EVENTS LOG SESSION
****************************************************************************************************
Session ID: 2024-01-15_14-30-25
Started At: 2024-01-15 14:30:25
Log File: /path/to/logs/domain-events/domain-events_2024-01-15_14-30-25.txt
****************************************************************************************************
```

### Event Entry

```
================================================================================
DOMAIN EVENT LOGGED
================================================================================
Timestamp: 2024-01-15 14:30:26.123
Event ID: 123e4567-e89b-12d3-a456-426614174000
Event Type: UserCreated
Occurred At: 2024-01-15T14:30:26.123Z
Actor ID: system
Created By: UserAggregate
Version: 1
Event Details: UserCreatedEvent(userId=..., userName=demo-user, platformCount=1)
================================================================================
```

### Session Footer

```
****************************************************************************************************
SESSION ENDED
Ended At: 2024-01-15 14:35:30
****************************************************************************************************
```

## Unterstützte Domain Events

- `UserCreatedEvent` - Benutzer wurde erstellt
- `ContentGeneratedEvent` - Inhalt wurde generiert
- `DistributionScheduledEvent` - Verteilung wurde geplant
- `DistributionCompletedEvent` - Verteilung erfolgreich abgeschlossen
- `DistributionFailedEvent` - Verteilung fehlgeschlagen

## Vorteile

1. **Audit Trail**: Vollständige Nachverfolgung aller Geschäftsereignisse
2. **Debugging**: Detaillierte Logs für Fehleranalyse
3. **Event Sourcing**: Grundlage für Event Sourcing-Architekturen
4. **Compliance**: Erfüllung von Audit-Anforderungen
5. **Analytics**: Basis für Geschäftsanalysen

## Integration in bestehende Anwendungen

Das Plugin ist so konzipiert, dass es nahtlos in bestehende Anwendungen integriert werden kann:

1. Abhängigkeit hinzufügen: `implementation(project(":plugins:textfile_logging_plugin"))`
2. Event Publisher ersetzen oder erweitern
3. Shutdown Hook für ordnungsgemäße Beendigung hinzufügen