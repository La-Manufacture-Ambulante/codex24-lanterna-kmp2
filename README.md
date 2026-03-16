# Lanterna (codex24 KMP fork)

This repository (`La-Manufacture-Ambulante/codex24-lanterna-kmp2`) is a Kotlin Multiplatform migration and publication track based on the original [`mabe02/lanterna`](https://github.com/mabe02/lanterna).

## What this repo is
- Upstream baseline: the original Java Lanterna project (`mabe02/lanterna`).
- This fork: migration and parity work for KMP in `kmp/`.
- Goal: publishable KMP artifacts while preserving Lanterna behavior and API intent.

## Where to start
- KMP workspace and commands: [`kmp/README.md`](kmp/README.md)
- Release process: [`kmp/docs/RELEASING.md`](kmp/docs/RELEASING.md)
- API compatibility policy: [`kmp/docs/API_COMPATIBILITY.md`](kmp/docs/API_COMPATIBILITY.md)
- Prepublication execution checklist: [`kmp/docs/PREPUBLICATION_CHECKLIST.md`](kmp/docs/PREPUBLICATION_CHECKLIST.md)

## Artifacts and coordinates
- Stable upstream Java artifact remains:
  - `com.googlecode.lanterna:lanterna:3.1.2`
- KMP branch/PR artifacts are currently validated through JitPack branch versions.
- Maven Central publication for KMP modules is prepared through the Gradle publishing/signing baseline in `kmp/`.

## Current support focus
- JVM (primary)
- Native parity targets in active CI and publishing flow:
  - `linuxX64`
  - `macosX64`
  - `macosArm64`
  - `mingwX64` (work in progress on some branches)

## Original Lanterna context

![Lanterna screenshot](http://mabe02.github.io/lanterna/resources/lanterna.png)

Lanterna is a Java library allowing you to write easy semi-graphical user interfaces in text terminals.
It offers three layers:
1. Low-level terminal API (`com.googlecode.lanterna.terminal`)
2. Screen buffer API (`com.googlecode.lanterna.screen`)
3. GUI toolkit (`com.googlecode.lanterna.gui2`)

## Discussions and docs
- Issues/PRs in this repository are the active channel for codex24 KMP work.
- Historical Lanterna discussion group: <https://groups.google.com/forum/#!forum/lanterna-discuss>
- Original development guide: <https://github.com/mabe02/lanterna/blob/master/docs/contents.md>
