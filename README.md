# README – HomeAutomationSystem

Dieses Dokument beschreibt, wie du die HomeAutomationSystem-Anwendung baust, startest und bedienst.

## Voraussetzungen

- Java 17 (JDK 17 oder neuer)

- Gradle Wrapper (im Projekt enthalten)

- Docker & Docker Compose (optional für Container-Betrieb)

## Lokaler Start (ohne Docker)

### Projekt bauen
- ./gradlew clean build

Resultat: Eine ausführbare JAR im Verzeichnis build/libs/.

### Anwendung starten

- java -jar build/libs/HomeAutomationSystem.jar

oder (bei generischem Namen)

- java -jar build/libs/*.jar

### UI öffnen
Nach dem Start öffnet sich automatisch die Swing-Benutzeroberfläche.

#### Bedienung der UI

Die Anwendung reagiert auf vordefinierte Befehle, die du direkt im Terminal eingibst:

- t value: Setzt die Temperatur-Simulation auf value °C (z.B. t 25.5).

- w condition: Setzt die Wetter-Simulation auf condition (z.B. w sunny oder w rain).

- ac on|off: Schaltet die Klimaanlage ein oder aus (z.B. ac on).

- ms on|off|play|stop: Steuert die MediaStation (Einschalten / Ausschalten / Abspielen / Stoppen) (z.B. ms play).

- sim mqtt|internal: Wechselt zwischen externer (MQTT) und interner Wetter-/Temperatur-Simulation (z.B. sim mqtt).

- quit: Beendet die Anwendung.

Ungültige Befehle oder falsche Parameter führen zu Usage-Hinweisen im Terminal. Aktuelle Statusmeldungen und Log-Informationen werden fortlaufend ausgegeben.