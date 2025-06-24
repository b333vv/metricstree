---
id: ARCH-service-god-object-metric-task-cache
title: "Legacy God Object: MetricTaskCache"
type: service
layer: application
owner: @team-arch
version: v1
status: current
created: 2024-07-29
updated: 2024-07-29
tags: [legacy, god-object, tech-debt]
depends_on: []
referenced_by: []
---
## Context

`MetricTaskCache` is an existing project service that acts as a "God Object", violating the Single Responsibility Principle. It is a central point of high coupling in the current architecture.

## Structure

This service combines three distinct responsibilities:
1.  **Task Queuing:** It manages a queue of background tasks (`Task.Backgroundable`) and controls their execution.
2.  **Results Caching:** It implements `UserDataHolder` to act as an in-memory cache for various calculation results.
3.  **VFS Listening:** It listens to file system events to invalidate its own caches.

## Evolution

### Planned
- This class is planned for complete removal. Its responsibilities will be migrated to the new `ARCH-service-task-queue` and `ARCH-service-cache` services during Phase 1 of the refactoring.

