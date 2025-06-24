---
id: ARCH-core-calculator
title: "Core Logic: Calculator Components"
type: component
layer: core
owner: @team-arch
version: v1
status: planned
created: 2024-07-29
updated: 2024-07-29
tags: [refactoring, core-logic, calculator]
depends_on: []
referenced_by: [ARCH-service-calculation]
---
## Context

This document describes the planned architectural pattern of using dedicated `Calculator` or `Builder` components to house the pure business logic of the plugin. This is a key part of the refactoring effort to decouple core logic from the IntelliJ Platform's execution framework.

## Structure

These components will be Plain Old Java Objects (POJOs) that take all necessary data and dependencies (e.g., PSI elements, settings) through their constructors or method parameters. They will contain the logic currently found inside `Task.Backgroundable` classes.

Example planned components: `ProjectTreeModelCalculator`, `PieChartDataCalculator`.

## Behavior

A `Calculator` will be instantiated and invoked by the `ARCH-service-calculation`. It will perform its specific computation (e.g., build a tree model, calculate metric distributions) and return the result as a pure data object. It will have no knowledge of background threads, progress indicators, or UI components.

## Evolution

### Planned
- Extract logic from all `*Task` classes into corresponding `*Calculator` or `*Builder` components as part of the Phase 2 refactoring.

