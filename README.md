# OpenAthena for Android
OpenAthena for Android

An Android port of the [OpenAthena project](http://OpenAthena.com)

OpenAthena allows consumer and professional drones to spot precise geodetic locations
🖼️👨‍💻 + 🧮⛰️ = 🎯📍


<a href="https://github.com/mkrupczak3/OpenAthena"><img width="540" alt="OpenAthena Drone Camera Terrain Raycast Concept Diagram" src="https://github.com/mkrupczak3/OpenAthena/raw/main/assets/OpenAthena_Concept_Diagram.png"></a>

# Stability
This project is not yet stable. Consider using the main [OpenAthena project](http://OpenAthena.com) running on a PC/Mac instead, until otherwise noted

# Operation Guide

## GeoTIFF Digital Elevation Model parsing:

Use of this app requires loading a GeoTIFF Digital Elevation Model (DEM) file, stored as a GeoTIFF ".tif" file.

GeoTIFF files store terrain elevation (height) for an area of the Earth. OpenAthena performs a ray-cast from a drone camera's position and orientation towards terrain. This may be used to precisely locate the subject which appears in the exact center of a given picture.

For information on how to clip a GeoTIFF file of a customized area, see [this link](https://github.com/mkrupczak3/OpenAthena/blob/main/EIO_fetch_geotiff_example.md).

Load the DEM file, e.g. [cobb.tif](https://github.com/mkrupczak3/OpenAthena/raw/main/src/cobb.tif) using the " ⛰" button (NOTE: during file selection, the thumbnail  image preview for any GeoTIFF ".tif" file will be blank. This is normal.), and the app will display the size of the file as well as its Latitude and Longitude boundaries:


<img height="270" alt="OpenAthena Android GeoTIFF DEM loading demo using cobb.tif" src="./assets/cobb_tif_DEM_Loading_Demo_landscape.png">


## JPG Drone sensor metadata parsing

This version of the app can only read sensor metadata from images taken by a DJI drone. The drone's position (Latitude Longitude Altitude) and its camera gimbal's azimuth and angle of depression are obtained from XMP metadata.

## Locating a Target 🎯

After loading a GeoTIFF DEM using the " ⛰" button (see section above), use the "🖼" button to select a drone image containing the necessary metadata.

<img height="270" alt="OpenAthena Android Image Selection demo using DJI_0419.JPG" src="./assets/DJI_0419_Image_Selection_Demo_landscape.png">

Then, press the "🧮" button to calculate the target location on the ground:

<img height="270" alt="OpenAthena Android Target Calculation demo using cobb.tif and DJI_0419.JPG" src="./assets/DJI_0419_Target_Res_Demo_landscape.png">


## Live Telemetry from DJI-SDK

**TBD**

# Contributing

If you're interested in contributing to this project, feel free to make a fork. This project will
follow the [fork and pull model](https://reflectoring.io/github-fork-and-pull/) for third-party contributors