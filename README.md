Color Buster Remake
===================

This is a minimalistic remake of the original Color Buster game for DOS
released in 1992. Time limit and accidental "death" are purposefully NOT
implemented.

# Prerequisites

The game is written in Java and implemented as an applet. It requires a
working Java Development Kit (JDK) with `appletviewer` support. The
latest version of OpenJDK that still supports `appletviewer` is 1.8.0.

Since Java is by definition a cross-platform environment, any host
platform that can run JDK is supported. The game was developed and
tested on Linux.

# Compiling

```
javac ColorGame.java
```

# Running

On Linux, it's easier to use the included wrapper script, like so:

```
./color.sh
```

On other platforms, run `appletviewer` directly, and include the full
path to `color.html`. The assumption is that the `PATH` environment
variable is set up correctly. For example:

```
appletviewer "C:\Games\color\color.html"
```

# How to play

When the game starts, it opens level 1 automatically. Click once inside
the game window to set focus and allow it to receive keyboard events.

## Switching to a different level

Right-click inside the game window. A pop-up menu will appear that can
be used to jump to a different level.

## Keyboard shortcuts

| Key(s)             | Description           |
| ------------------ | --------------------- |
| `↑`, `↓`, `←`, `→` | Move character around |
| `Space`            | Go to the next level  |
| `Esc`              | Restart current level |
| `U` or `Backspace` | Undo move             |
| `R`                | Redo move             |
