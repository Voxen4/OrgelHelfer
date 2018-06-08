# Orgelhelfer
Der Orgelhelfer ist ein Projekt für das Fach "Anwendungen für Mobile Systeme" an der Ostfalia Hochschule für angewandte Wissenschaften 
im Bereich Informatik. Die App sollte für die St. Katharinen Kirche in Braunschweig entwickelt werden um den Organisten beim Spielen der Orgel zu entlasten.
Die App soll dem Organisten helfen die Register während des Spielen an der Orgel zu ändern. Dies soll über eine Midi-Schnittstelle realisiert werden
Die Orgel ist elektronisch aufgebaut und verfügt über eine SPS-Schnittstelle, die über ein Controller angesprochen wird.

Während der Projektphase wurde das Programm auf einem E-Keyboard umgesetzt. Dort ist relativ schnell deutlich geworden, dass das Umschalten  der Register auf einem Keyboard nicht funktioniert, wie es beschrieben worden ist. Gleichzeitig wurde beim Testen an der Orgel festgestellt, dass das Protokoll für die SPS-Steuerung neu wäre und die Einarbeitungszeit den Projektrahmen sprengen würde.
Aus diesen Gründen wurden die Anforderungen an das Projekt geändert. Die neuen Anforderungen besagen, dass eine Art Spotify für Midi Songs 
entwickelt werden würde, mit dem Unterschied, dass es eine Funktion zum automatischen Weiterspielen eingefügt werden soll. Dies wird
über den Timewarp Algorithmus realisiert. 




## Funktionen des Programms
* Abspielen/Aufnehmen von Midi-Songs
* Speichern der Daten in Playlisten und Kategorien
* Speichern der Daten in einer Room Datenbank
* Automatisches Weiterspielen von Songs
* Dynamic Timewarp Algortithmus für das Weiterspielen


## Referenzen/Frameworks/Libarys
* [RecyclerView](https://github.com/h6ah4i/android-advancedrecyclerview) - Kategorie/Playlist Layout
* [Room Database](https://developer.android.com/topic/libraries/architecture/room) - Datenbank für Kategorien/Playlisten/Tracks


## Bedienung der App
Die App wurde bewusst simpel aufgebaut, da der Organist im fortgeschrittenen Alter ist. 

### Main Activity
![alt text](https://imgur.com/a/VmtqXz2)
