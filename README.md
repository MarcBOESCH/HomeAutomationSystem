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

## Docker-Betrieb

### Voraussetzungen für Docker
- Docker und Docker Compose installiert
- Für die GUI: X-Server unter Windows (z.B. VcXsrv oder Xming)

### Mit Docker starten
1. X-Server starten (nur für Windows):
   - VcXsrv oder Xming starten
   - "Disable access control" aktivieren

2. Container bauen und starten:
   ```
   docker-compose up --build
   ```

3. Zum Beenden:
   ```
   docker-compose down
   ```

### Hinweise zum Docker-Betrieb
- Die Anwendung läuft in einem Container mit GUI-Unterstützung
- Eingaben können direkt im Terminal erfolgen, in dem docker-compose ausgeführt wurde
- Bei Problemen mit der GUI-Anzeige, bitte die X-Server-Einstellungen überprüfen

### UI öffnen
Nach dem Start öffnet sich automatisch die Swing-Benutzeroberfläche.

#### Bedienung der UI

Die Anwendung reagiert auf vordefinierte Befehle, die du direkt im Terminal eingibst:

- t value: Setzt die Temperatur-Simulation auf value °C (z.B. t 25.5).

- w condition: Setzt die Wetter-Simulation auf condition (z.B. w sunny oder w rain).

- ac on|off: Schaltet die Klimaanlage ein oder aus (z.B. ac on).

- ms on|off|play|stop: Steuert die MediaStation (Einschalten / Ausschalten / Abspielen / Stoppen) (z.B. ms play).

- sim mqtt|internal: Wechselt zwischen externer (MQTT) und interner Wetter-/Temperatur-Simulation (z.B. sim mqtt).

- sf order <productname> <?zahlenwert> /auto <productname> <?zahlenwert>/eat/look/history:   
   order: Bestellen eines Produktes. (optional mit Zahlenwert, damit mehr bestellt werden)      
   auto: Automatisches bestellen eines Produktes falls es leer ist (optional mit Zahlenwert, damit mehr bestellt werden können)   
   eat: Konsumieren eines Produktes, dass sich im Kühlschrank befindet.   
   look: Auflistung der Produkte im Kühlschrank.   
   history: Auflistung der gesamten Bestellhistorie.   

- quit: Beendet die Anwendung.

Ungültige Befehle oder falsche Parameter führen zu Usage-Hinweisen im Terminal. Aktuelle Statusmeldungen und Log-Informationen werden fortlaufend ausgegeben.
