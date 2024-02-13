# OpenAthena™ for Android
OpenAthena for Android

An Android port of the [OpenAthena project](http://OpenAthena.com)

OpenAthena™ allows common drones to spot precise geodetic locations.

🖼️👨‍💻 + 🧮⛰️ = 🎯📍


<a href="https://github.com/mkrupczak3/OpenAthena"><img width="540" alt="OpenAthena Drone Camera Terrain Raycast Concept Diagram" src="https://github.com/mkrupczak3/OpenAthena/raw/main/assets/OpenAthena_Concept_Diagram.png"></a>

<a href="https://play.google.com/store/apps/details?id=com.openathena"><img width="330" alt="OpenAthena arbitrary point location demo gif" src="./assets/tap_to_locate_demo_small.gif"></a>

<a href="https://play.google.com/store/apps/details?id=com.openathena"><img width="586" alt="OpenAthena Android splash screen demo" src="./assets/App_Open_Demo_landscape.png"></a>

<img width="586" alt="OpenAthena™ Android Target Calculation demo using cobb.tif and DJI_0419.JPG, output mode WGS84" src="./assets/DJI_0419_Target_Res_Demo_landscape.png">

<img width="586" alt="OpenAthena Android DJI_0419.JPG target shown in Google Maps satellite view" src="./assets/0419_maps_screenshot.png">

<img width="586" alt="OpenAthena for Android triggers a waypoint to show in Android Team Awarness Kit at the calculated location" src="./assets/ATAK_OpenAthena_CoT_Demo_landscape.png">

# License

Copyright (C) 2024 Theta Informatics LLC

This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

# Install

<a href="https://f-droid.org/packages/com.openathena">
    <img src="https://fdroid.gitlab.io/artwork/badge/get-it-on.png"
    alt="Get it on F-Droid"
    height="80">
</a>
<a href='https://apt.izzysoft.de/fdroid/index/apk/com.openathena'><img width="216" alt='Get it on izzysoft.de' src='https://gitlab.com/IzzyOnDroid/repo/-/raw/master/assets/IzzyOnDroid.png'/></a>
<a href='https://play.google.com/store/apps/details?id=com.openathena&pcampaignid=pcampaignidMKT-Other-global-all-co-prtnr-py-PartBadge-Mar2515-1'><img width="216" alt='Get it on Google Play' src='https://play.google.com/intl/en_us/badges/static/images/badges/en_badge_web_generic.png'/></a>

## GitHub releases page:
[https://github.com/Theta-Limited/OpenAthenaAndroid/releases](https://github.com/Theta-Limited/OpenAthenaAndroid/releases)


# Operation manual



## Obtain a GeoTIFF Digital Elevation Model:

To use this app, you need a GeoTIFF or DTED (resolution 2 or higher) Digital Elevation Model (DEM) file. DEM files store terrain elevation data for an area on Earth. OpenAthena performs a ray-cast from a drone camera's position and orientation towards the terrain, which can be used to precisely locate any point within a given drone image.

To obtain a GeoTIFF file for a certain area, use [this link](https://github.com/mkrupczak3/OpenAthena/blob/main/EIO_fetch_geotiff_example.md).

## Load a GeoTIFF Digital Elevation Model  ⛰:

Load the DEM file (e.g. cobb.tif) using the "⛰" button. The app will display the size of the file and its latitude and longitude boundaries:


(NOTE: during file selection, the thumbnail  image preview for any GeoTIFF ".tif" or DTED ".dt#" file will be blank. This is normal.)


<img width="586" alt="OpenAthena™ Android GeoTIFF DEM loading demo using cobb.tif" src="./assets/cobb_tif_DEM_Loading_Demo_landscape.png">

## Setup for drone flight

### Compass sensor 🧭 calibration

It is _**strongly suggested**_ that you should [calibrate the drone's compass sensor for the local environment](https://phantompilots.com/threads/compass-calibration-a-complete-primer.32829/) before taking photos to be used with OpenAthena. Consult your drone's operation manual for this procedure. The image metadata from an un-calibrated drone can be several degrees off from the correct heading. This can result in dramatic target-resolution inaccuracies if the sensor is not calibrated. _**Always**_ verify a target match location from OpenAthena before use!

E.g.:

<img width="586" alt="OpenAthena Android an example of a bad target resolution due to an un-calibrated magnetometer compass sensor" src="./assets/magnetometer_fail.png">

#### Optional: use the "Manual Azimuth Correction" slider to correct bad compass data

If you find your aircraft's compass sensor is still not providing correct heading information, you can use this slider to manually apply a configurable offset anywhere in the range of [-15.0°, +15.0°]. This offset will be added to your aircraft's camera heading before target calculation is performed:

<img width="330" alt="OpenAthena Android Manual Azimuth Correction Slider" src="./assets/Settings_Manual_Azimuth_Correction_Demo.png">

**NOTE:** This value is _**NOT**_ for setting [magnetic declination](https://ngdc.noaa.gov/geomag/declination.shtml)! Magnetic declination is already accounted for by your drone's onboard digital World Magnetic Model (WMM). Improper use of this Manual Offset setting will result in bad target calculation output.

Your selected manual correction value is saved automatically between launches of the app. To reset the value, tap the "RESET" button in the Settings screen or move the slider to the middle.

### Let your drone acquire GPS lock before flying

For the best results for target calculation, it's important to let your drone sit at the launch position until it can get an accurate GPS fix. This is important for it to be able measure altitude correctly during flight.

On DJI drones, this indicator shows the number of GPS satellites visible to the drone:

<img width="586" alt="A screenshot of the UI for DJI Go 4 during flight of a Mavic 2 Zoom drone. The GPS connection indicator is highlighted" src="./assets/dji_good_gps_lock_ex.png">

Wait until at least 6 GPS satellites are visible (or you can confirm the GPS fix is good) before starting flight.

## Select an Image 🖼:

This app is compatible with images taken by select models of DJI, Skydio, Autel, and Parrot aircraft models. The drone's position and its camera's orientation are automatically extracted from the image metadata.

After loading a GeoTIFF DEM, use the "🖼" button to select a drone image containing the necessary metadata:

<img width="586" alt="OpenAthena™ Android Image Selection demo using DJI_0419.JPG" src="./assets/DJI_0419_Image_Selection_Demo_landscape.png">

## Calculate a target 🎯:

Tap anywhere on the displayed image to calculate the corresponding target location on the ground. You can tap the result display box to copy the result text to your clipboard or open the position in Google Maps by clicking the blue hyperlink:

<img width="586" alt="OpenAthena™ Android Target Calculation demo using cobb.tif and DJI_0419.JPG, output mode WGS84" src="./assets/DJI_0419_Target_Res_Demo_landscape.png">

<img width="586" alt="OpenAthena Android DJI_0419.JPG target location text copied to clipboard" src="./assets/0419_text_copied_to_clipboard.png">

<!-- <img width="340" alt="Gif showing text copied to clipboard toast after taping on results box" src="./assets/TextCopiedOptimized.gif"> -->

<img width="586" alt="OpenAthena Android DJI_0419.JPG target shown in Google Maps satellite view" src="./assets/0419_maps_screenshot.png">

## [ATAK](https://en.wikipedia.org/wiki/Android_Team_Awareness_Kit) Cursor on Target

When the "✉️" button is pressed, OpenAthena will automatically send a Cursor on Target multicast UDP packet to udp://239.2.3.1:6969 to all devices connected via network. Under default settings, this will cause a marker to show up in ATAK at the target location for all recipients:

<img width="586" alt="OpenAthena for Android triggers a waypoint to show in Android Team Awarness Kit at the calculated location" src="./assets/ATAK_OpenAthena_CoT_Demo_landscape.png">

Change the marker to its appropriate type (friend, suspect, hostile) then send the target to other networked users.

## Arbitrary Point Selection

OpenAthena allows users to tap any point in the image to locate it. Tapping on any point in the image will move the marker and calculate the new location. A new Cursor-on-Target message will not be sent to ATAK until the "✉️" button is pressed:

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


Then press the back button or again tap the kebab menu icon (three dots) to return to the "Calculate" screen:

<img width="270" alt="OpenAthena™ Android Target Calculation demo using cobb.tif and DJI_0419.JPG, output mode NATO MGRS 10m" src="./assets/DJI_0419_Target_Res_MGRS10m_Demo.png">


## Telemetry and localization from live video feed

The end goal of this project is to enable UAS operators to perform terrain-raycast localization from any point within live video feed. The target resolution engine of this application is highly modular, and may be extended to operate on any such telemetry data source. This capability will be specific to each UAS manufactuer's SDK however, therefore this capability will take time to develop.

# Contributing

## Code
If you're interested in contributing to this project, feel free to make a fork. This project will
follow the [fork and pull model](https://reflectoring.io/github-fork-and-pull/) for third-party contributors.

### Cloning this project with submodules
This project requires content from other git repositories to build correctly.

To clone this project properly, with the necessary submodules:
```bash
git clone --recursive git@github.com:Theta-Limited/OpenAthenaAndroid.git # can also use https clone url instead
```

If you have already cloned, and wish to acquire the neccesary submodule contents:
```bash
git submodule init
git submodule update
```

If you wish to update the contents of submodules from their source:
```bash
git submodule update --remote
```

# Acknowledgements

This software project would not be possible without the tireless work of many U.S. public servants and open source maintainers. Please see [CREDITS.md](./CREDITS.md) for a full list of included software libraries, and their authors and licenses.
