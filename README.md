# Tresure Hunt

A fun trasure hunt app developed by students that forces you to go out and explore the many different waypoints your friends set for you to find.

## Features

- Share waypoints through QR or Code Import.
- Compass Arrow pointing to the Treasure.
- Achievement and in-game currency.
- Different themes selection for UI.

## Prerequisites

Before starting, ensure you have the following installed and ready:
- [Android Studio] (https://developer.android.com/studio) - Latest is recommended.
- Google maps API Key (In an .env in main project folder).
- Set a local.properties in main project folder if not there. 

## Environment Configuration

An `.env` file will needs to be in the main project folder. Input the Google Maps api Key there.

```bash
API_KEY=your_google_maps_api_key_here
```

Next make sure to set your local.properties in main project folder. 

```local.properties
#local.properties
sdk.dir=C:\\Users\\<your-username>\\AppData\\Local\\Android\\Sdk   # Windows
sdk.dir=/Users/<your-username>/Library/Android/sdk                # macOS
```

## Local Development Setup

### 1. Clone the Repository
```bash
git clone <repository url>
cd gps_app
```

### 2. Sync Gradle

If not already done, download Android Studio and all their required dependencies through it automatically. Refer to Prerequisites.

When opening the project Gradle should ask you to sync.

### 3. Configure Environment
1. Copy the example environment file:
```bash
  cd gps_app
```
2. You should find the .env example in environment configuration, copy this and make a .env and add you Google Maps Api key.

### 4. Connect app to Android Phone

1. With a usb cable connect to your Android device and Pair with wifi if not connected automatically and run the app on your laptop. It should download the app on your phone not permanent though.

## Contributers for this Project

Students from NHL Stenden:
  - Mihail Druzeta
  - Jia Men Lam
  - Thijs
  - Fjodor Smorodins
  - Zhi Cheng Liang

## Troubleshooting

### Common Issues

1. **No Target Device Found**
  - If you are on a Desktop or PC check if you have a virtual device, if not then go to the right hand side and add a virtual device on Android Studio. (Medium Phone is recommended).

2. **App not Starting**
  - Make sure you have a valid Google Maps API key in a .env, local.properties should be set automatically.



