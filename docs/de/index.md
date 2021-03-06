---
title: Dokumentation
subtitle: Json Metrics Plugin Dokumentation
---

Diese Dokumentation beschreibt die Nutzung des Json Metrics Plugin. Sie steht in verschiedenen Sprachen zur Verfügung, die auf der rechten Seite gewechselt werden können.

In SCM-Manager sammeln viele Komponenten Metriken, die nützliche Informationen bereitstellen und Aufschluss über die Nutzung des Systems geben können. Dieses Plugin stellt einen Endpunkt zur Verfügung, der die gesammelten Metriken im JSON-Format bereitstellt.

# Nutzung

Die URL zu diesem Endpunkt kann über den Index (`/api/v2`) der jeweiligen SCM-Manager Instanz erfragt werden. Der Link befindet sich unter `_links.metrics` mit dem Namen `json`.

Um den Endpunkt abzufragen, wird eine gültige Authentifizierung benötigt. Wenn der Endpunkt in ein externes System eingebunden werden soll, empfiehlt es sich für die Authentifizierung einen technischen Account in SCM-Manager anzulegen.
