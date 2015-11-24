# Plarpebu

## Description

Plarpebu is a Java karaoke player that runs on any architecture that can run java. It plays
both midi (.mid and .kar) and mp3+cdg files. 

This repository is a clone of the subversion repository (through revision 178) hosted at 
[sourceforge.net/projects/plarpebu](http://sourceforge.net/projects/plarpebu/). That project
was last updated in 2007 when Java 6 was the current version of the platform. When Java 7
was released a new method was added to java.awt.Window that conflicted with the plugin
framework used by Plarpebu, breaking the application for anyone using Java 7+. This repository 
was created to fix those problems, and to provide a better environment for collaboration.

[Release notes](release_notes.md).

## Directory Structure

    plarpebu/
    |-- AlbumGrabberPlugin   (Plugin module)
    |-- BasicPlugins         (Plugin module)
    |-- build/distributions  (Directory where the distributable zip is created)
    |-- build.gradle         (Gradle build file)
    |-- CdgPlugin            (Plugin module)
    |-- Common               (Plugin module)
    |-- includes             (contains files that are included in a distributable release archive)
    |-- ExamplePlugins       (Plugin module)
    |-- gradlew              (Gradle Wrapper - *nix, osx)
    |-- gradlew.bat          (Gradle Wrapper - Windows)
    |-- Player               (Main Application module)
    |-- PluginLoaderAPI      (Module - used by Player to load the plugins at runtime)
    |-- PluginsSDK           (Module - used by Player to load the plugins at runtime)
    |-- README.md            (This file)
    |-- release_notes.md     (What changed between versions)
    |-- settings.gradle      (Gradle build file)
    `-- VisualizerPlugin     (Plugin module)

## How to build the project

### Prerequisite Software
1. [Java 8+](http://www.oracle.com/technetwork/java/javase/downloads/index.html)
1. [Gradle](http://gradle.org/gradle-download/)

### Build
      $ gradle clean releasePlarpebu

Running this command will produce a distributable artifact (Plarpebu-\<version\>.zip)
located in the \<project_root\>/build/distributions directory.

### Install

Extract the distributable artifact (Plarpebu-\<version\>.zip) to a directory of your choice. 
This will produce the following directory structure:

    Plarpebu-<version>
    |-- demo         (Can be deleted. It contains example karaoke files and a saved example playlist.)
    |-- icons        (Images used by the application. Needs to be refactored.)
    |-- libs         (Application dependency libraries)
    |-- player.jar   (Main Application)
    |-- plugins      (Directory for plugins)
    |-- preferences  (Direcotry for persistent application preferences)
    |-- run.bat      (Run script for Windows)
    |-- run.sh       (Run script for *nix/osx)
    `-- skins        (UI theme packs)

### Run

    # On *nix, osx operating systems:
    $ ./run.sh
    
    # On Windows operating systems:
    $ ./run.bat

## Bug Reports/Feature Requests

TODO: Create automatic build and issue tracker on github.

## Known Issues

TODO: Document the several known issues.