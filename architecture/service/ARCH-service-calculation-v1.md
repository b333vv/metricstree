---
id: ARCH-service-calculation
title: "Calculation Service"
type: service
layer: application
owner: @team-arch
version: v1
status: planned
created: 2024-07-29
updated: 2024-07-29
tags: [refactoring, service, calculation]
depends_on: [ARCH-service-task-queue, ARCH-service-cache, ARCH-service-settings, ARCH-core-calculator]
referenced_by: []
---
## Context

This planned service will act as a high-level facade for initiating all metric calculation workflows. It will abstract away the complexities of caching, background task management, and result publication from the UI layer.

## Structure

The `CalculationService` will be a project-level service with a simple, declarative API (e.g., `calculateProjectTree()`, `calculatePieChart()`). It will depend on the `TaskQueueService`, `CacheService`, and `SettingsService` to perform its duties.

## Behavior

When a method on the `CalculationService` is called (typically by a UI `AnAction`), it will:
1. Check the `CacheService` for a valid, non-stale result. If found, publish it to the `MessageBus` and return.
2. If no valid cache exists, it will instantiate the appropriate `Calculator` component from the core logic layer.
3. It will wrap the call to the calculator in a generic background task and queue it using the `TaskQueueService`.
4. The task's `onSuccess` callback will be responsible for publishing the result to the `MessageBus` and storing it in the `CacheService`.

## Evolution

### Planned
- This service will be created during Phase 2 of the refactoring to decouple the UI from the core logic.

