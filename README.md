# OpenAthena™ for Android
OpenAthena for Android

An Android port of the [OpenAthena project](http://OpenAthena.com)

OpenAthena™ allows consumer and professional drones to spot precise geodetic locations.

🖼️👨‍💻 + 🧮⛰️ = 🎯📍


<a href="https://github.com/mkrupczak3/OpenAthena"><img width="540" alt="OpenAthena Drone Camera Terrain Raycast Concept Diagram" src="https://github.com/mkrupczak3/OpenAthena/raw/main/assets/OpenAthena_Concept_Diagram.png"></a>

<a href="https://play.google.com/store/apps/details?id=com.openathena"><img width="330" alt="OpenAthena arbitrary point location demo gif" src="./assets/tap_to_locate_demo_small.gif"></a>

<a href="https://play.google.com/store/apps/details?id=com.openathena"><img width="586" alt="OpenAthena Android splash screen demo" src="./assets/App_Open_Demo_landscape.png"></a>

<img width="586" alt="OpenAthena™ Android Target Calculation demo using cobb.tif and DJI_0419.JPG, output mode WGS84" src="./assets/DJI_0419_Target_Res_Demo_landscape.png">

<img width="586" alt="OpenAthena Android DJI_0419.JPG target shown in Google Maps satellite view" src="./assets/0419_maps_screenshot.png">

<img width="586" alt="OpenAthena for Android triggers a waypoint to show in Android Team Awarness Kit at the calculated location" src="./assets/ATAK_OpenAthena_CoT_Demo_landscape.png">

<a href='https://play.google.com/store/apps/details?id=com.openathena&pcampaignid=pcampaignidMKT-Other-global-all-co-prtnr-py-PartBadge-Mar2515-1'><img width="216" alt='Get it on Google Play' src='https://play.google.com/intl/en_us/badges/static/images/badges/en_badge_web_generic.png'/></a>

# Operation Guide



## Obtain a GeoTIFF Digital Elevation Model:

To use this app, you need a GeoTIFF Digital Elevation Model (DEM) file. GeoTIFF files store terrain elevation data for an area on Earth. OpenAthena performs a ray-cast from a drone camera's position and orientation towards the terrain, which can be used to precisely locate any point within a given picture.

To obtain a GeoTIFF file for a certain area, use [this link](https://github.com/mkrupczak3/OpenAthena/blob/main/EIO_fetch_geotiff_example.md).

## Load a GeoTIFF Digital Elevation Model  ⛰:

Load the DEM file (e.g. cobb.tif) using the "⛰" button. The app will display the size of the file and its latitude and longitude boundaries:


(NOTE: during file selection, the thumbnail  image preview for any GeoTIFF ".tif" file will be blank. This is normal.)


<img width="586" alt="OpenAthena™ Android GeoTIFF DEM loading demo using cobb.tif" src="./assets/cobb_tif_DEM_Loading_Demo_landscape.png">

## Calibrate your drone's compass sensor 🧭 and take photos :

It is _**strongly suggested**_ that you should [calibrate the drone's compass sensor for the local environment](https://phantompilots.com/threads/compass-calibration-a-complete-primer.32829/) before taking photos to be used with OpenAthena. Consult your drone's operation manual for this procedure. The image metadata from an un-calibrated drone can be several degrees off from the correct heading. This can result in dramatic target-resolution inaccuracies if the sensor is not calibrated. _**Always**_ verify a target match location from OpenAthena before use!

E.x.:

<img width="586" alt="OpenAthena Android an example of a bad target resolution due to an un-calibrated magnetometer compass sensor" src="./assets/magnetometer_fail.png">


## Select an Image 🖼:

This app is compatible with images taken by select models of DJI, Skydio, Autel, and Parrot aircraft models. The drone's position and its camera's orientation are automatically extracted from the image metadata.

After loading a GeoTIFF DEM, use the "🖼" button to select a drone image containing the necessary metadata:

<img width="586" alt="OpenAthena™ Android Image Selection demo using DJI_0419.JPG" src="./assets/DJI_0419_Image_Selection_Demo_landscape.png">

## Calculate a target 🧮 🎯:

Press the "🧮" button to calculate the target location on the ground. You can tap the result display box to copy the result text to your clipboard or open the position in Google Maps by clicking the blue hyperlink:

<img width="586" alt="OpenAthena™ Android Target Calculation demo using cobb.tif and DJI_0419.JPG, output mode WGS84" src="./assets/DJI_0419_Target_Res_Demo_landscape.png">

<img width="586" alt="OpenAthena Android DJI_0419.JPG target location text copied to clipboard" src="./assets/0419_text_copied_to_clipboard.png">

<img width="586" alt="OpenAthena Android DJI_0419.JPG target shown in Google Maps satellite view" src="./assets/0419_maps_screenshot.png">

## [ATAK](https://en.wikipedia.org/wiki/Android_Team_Awareness_Kit) Cursor on Target

When the "🧮" button is pressed, OpenAthena will automatically send a multicast packet to udp://239.2.3.1:6969 . Under default settings, this will cause a marker to show up in ATAK at the target location:

<img width="586" alt="OpenAthena for Android triggers a waypoint to show in Android Team Awarness Kit at the calculated location" src="./assets/ATAK_OpenAthena_CoT_Demo_landscape.png">

Change the marker to its appropriate type (friend, suspect, hostile) then send the target to other networked users.

## Arbitrary Point Selection

OpenAthena allows users to tap any point in the image to locate it. Tapping on any point in the image will move the marker and calculate the new location. A new Cursor-on-Target message will not be sent to ATAK until the "🧮" button is pressed:

<img width="586" alt="OpenAthena for Android demo of arbitrary point selection for raycast calculation" src="./assets/DJI_0419_Target_Res_Arbitrary_Point_Demo_landscape.png">

<img width="586" alt="OpenAthena for Android demo of a cursor on target message calculated for an arbitrary point selected in a drone image" src="./assets/ATAK_OpenAthena_CoT_Arbitrary_Point_Demo_landscape.png">

# Application Settings (optional) ⚙:

OpenAthena for Android supports multiple output modes for target calculation, including:

* Latitude, Longitude (standard WGS84)
* [Nato Military Grid Reference System](https://en.wikipedia.org/wiki/Military_Grid_Reference_System) (MGRS) 1m, 10m, and 100m
* [CK-42 Система координат](https://en.wikipedia.org/wiki/SK-42_reference_system) Latitude Longitude (an alternative geodetic system commonly used in slavic countries)
* [CK-42 Система координат](https://en.wikipedia.org/wiki/SK-42_reference_system) [Gauss-Krüger](https://desktop.arcgis.com/en/arcmap/latest/map/projections/gauss-kruger.htm) Grid: Northing, Easting (an alternative military grid reference system used by former Warsaw pact countries)

To change the ouptut mode of OpenAthena for Android, tap the kebab menu icon (three dots) at the top-right corner of the screen and select "Settings":

<img width="586" alt="OpenAthena™ Android Open Settings Activity demo" src="./assets/DJI_0419_Open_Settings_Demo_landscape.png">

<img width="270" alt="OpenAthena™ Android 🎯 Output Modes Activity demo WGS84" src="./assets/Settings_WGS84_Demo.png">

Select your desired output mode by pressing its button in the list:

<img width="270" alt="OpenAthena™ Android 🎯 Output Modes Activity demo NATO MGRS 10m" src="./assets/Settings_MGRS10m_Demo.png">


Then press the back button or again tap the kebab menu icon (three dots) to return to the "Calculate" screen. Finally, press the "🧮" button to re-calculate the target location according to your chosen output mode:

<img width="270" alt="OpenAthena™ Android Target Calculation demo using cobb.tif and DJI_0419.JPG, output mode NATO MGRS 10m" src="./assets/DJI_0419_Target_Res_MGRS10m_Demo.png">


## LIVE Telemetry from DJI-SDK

**TBD**

# Contributing
## UI language translation
If you speak another language in addition to English, please consider contributing to the UI translation using the link below. The insights of a native-speaker are the only way to provide a good user experience for the program in a given language:

[https://www.transifex.com/krupczakorg/openathena-for-android/](https://www.transifex.com/krupczakorg/openathena-for-android/)


## Code
If you're interested in contributing to this project, feel free to make a fork. This project will
follow the [fork and pull model](https://reflectoring.io/github-fork-and-pull/) for third-party contributors
