---
id: ARCH-service-cache
title: "Cache Service"
type: service
layer: application
owner: @team-arch
version: v1
status: planned
created: 2024-07-29
updated: 2024-07-29
tags: [refactoring, service, cache]
depends_on: []
referenced_by: [ARCH-service-calculation]
---
## Context

This planned service will be responsible for all in-memory caching of metric calculation results. It will centralize the logic currently spread within `MetricTaskCache`.

## Structure

The `CacheService` will be a project-level service implementing `UserDataHolder`. It will define all `Key` constants for cached data (e.g., `PROJECT_TREE`, `CLASS_AND_METHODS_METRICS`). It will also contain the VFS listener logic to invalidate caches when source files change.

## Behavior

The service will provide simple `get/put` methods for storing and retrieving cached data. The `CalculationService` will be its primary client. The internal VFS listener will automatically clear caches when relevant files are modified, ensuring data consistency.

## Evolution

### Planned
- This service will be created during Phase 1 of the refactoring, extracting the caching and VFS listening responsibilities from `MetricTaskCache`.

