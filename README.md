# Cravify

Cravify is an Android food ordering and restaurant management application designed for a mobile-first food discovery and ordering experience. The app supports multiple user types, including customers, restaurant partners, and admins, with Firebase-backed authentication and cloud data storage.

This project is built using Java in Android Studio and is structured around a modular app flow with login screens, restaurant dashboard management, cart functionality, maps integration, and notification support.

## Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [Application Flow](#application-flow)
- [Screens and Modules](#screens-and-modules)
- [Prerequisites](#prerequisites)
- [Setup and Installation](#setup-and-installation)
- [Firebase Configuration](#firebase-configuration)
- [Google Maps Configuration](#google-maps-configuration)
- [Permissions](#permissions)
- [Build and Run](#build-and-run)
- [Usage](#usage)
- [Known Notes](#known-notes)
- [License](#license)

---

## Overview

Cravify is a food and restaurant management app that allows:

- Users to browse restaurants and food items
- Add items to a cart
- View saved restaurants and order history
- Log in securely with Firebase Authentication
- Partner restaurants to manage their profiles and menu
- Admins to oversee restaurant-related operations
- Users to find restaurants via location-based maps
- Push notifications for events and updates

The app uses Firebase for authentication, real-time database/firestore operations, cloud storage, and messaging, and integrates with Google Maps, Places APIs, and Razorpay payment gateway.

---

## Features

### Customer Features
- User sign-up and sign-in
- Restaurant discovery and food browsing
- Add/remove food items from cart
- View saved favorites
- Order history tracking
- Profile management
- Location-based restaurant discovery
- Intro screen and onboarding flow
- Push notifications

### Restaurant Partner Features
- Restaurant registration
- Partner login
- Restaurant dashboard
- Restaurant profile and address details
- Menu management flow
- Order-related operations
- Profile and business information management

### Admin Features
- Admin login portal
- Admin dashboard
- Access to restaurant/user management flows

### Additional Features
- Splash screen animation
- Material UI
- Google Maps integration
- Razorpay payment integration
- Image loading and display via Glide
- Modern Android UI styling and animations

---

## Tech Stack

This project uses the following technologies:

- Java
- Android SDK
- AndroidX / Material Components
- Firebase Authentication
- Firebase Firestore
- Firebase Storage
- Firebase Cloud Messaging
- Google Maps SDK
- Google Places API
- Razorpay Checkout
- Glide for image loading
- ViewPager / Intro Slider UI
- Gradle Kotlin DSL

### Android Configuration
- Compile SDK: 34
- Minimum SDK: 27
- Target SDK: 34
- App Package: `com.example.cravify`

---

## Project Structure

Cravify/
├── .idea/                          # Android Studio project metadata
├── app/
│   ├── src/
│   │   ├── androidTest/java/com/example/cravify/
│   │   │   └── ExampleInstrumentedTest.java
│   │   ├── main/
│   │   │   ├── java/com/example/cravify/
│   │   │   │   ├── AdminDashboard.java
│   │   │   │   ├── AdminLogin.java
│   │   │   │   ├── Cart.java
│   │   │   │   ├── CartAdapter.java
│   │   │   │   ├── CartItem.java
│   │   │   │   ├── HistoryFragment.java
│   │   │   │   ├── HomeFragment.java
│   │   │   │   ├── IntroSlider.java
│   │   │   │   ├── IntroViewPagerAdapter.java
│   │   │   │   ├── MainActivity.java
│   │   │   │   ├── MapsActivity.java
│   │   │   │   ├── MenuAdapter.java
│   │   │   │   ├── MenuItem.java
│   │   │   │   ├── MyFirebaseMessagingService.java
│   │   │   │   ├── OrderHistoryAdapter.java
│   │   │   │   ├── OrderHistoryItem.java
│   │   │   │   ├── ProfileFragment.java
│   │   │   │   ├── Restaurant.java
│   │   │   │   ├── RestaurantAdapter.java
│   │   │   │   ├── RestaurantDashboard.java
│   │   │   │   ├── RestaurantLogin.java
│   │   │   │   ├── RestaurantMenuActivity.java
│   │   │   │   ├── RestaurantRegistration.java
│   │   │   │   ├── RestaurantsAdapter.java
│   │   │   │   ├── SavedFragment.java
│   │   │   │   ├── ScreenItem.java
│   │   │   │   ├── SliderAdapter.java
│   │   │   │   ├── Splash.java
│   │   │   │   ├── UserLogin.java
│   │   │   │   ├── UserRegistration.java
│   │   │   │   ├── UsersAdapter.java
│   │   │   │   ├── ViewPagerAdapter.java
│   │   │   │   └── ...
│   │   │   ├── AndroidManifest.xml
│   │   │   ├── ic_launcher-playstore.png
│   │   │   └── res/
│   │   │       ├── anim/
│   │   │       ├── color/
│   │   │       ├── drawable/
│   │   │       ├── font/
│   │   │       ├── layout/
│   │   │       ├── menu/
│   │   │       ├── mipmap*/
│   │   │       ├── raw/
│   │   │       ├── values/
│   │   │       └── xml/
│   │   └── test/java/com/example/cravify/
│   │       └── ExampleUnitTest.java
│   ├── .gitignore
│   ├── build.gradle.kts
│   ├── google-services.json
│   └── proguard-rules.pro
├── build.gradle.kts
├── gradle.properties
├── gradle/
│   └── wrapper/
├── gradlew
├── gradlew.bat
├── settings.gradle.kts
├── .gitignore
└── README.md

Application Flow
The app starts with a splash screen and intro flow, then leads the user to authentication:

Splash activity plays the splash animation
If the intro has not been opened before, user is shown IntroSlider
User logs in from UserLogin
After successful login, user is redirected to MainActivity
MainActivity hosts bottom navigation tabs:
Home
Saved
History
Profile
Users can browse food and restaurant items, add them to cart, and proceed to orders
Restaurant owners and admins have dedicated login/dashboard flows
Screens and Modules
Core Activities
Splash.java
IntroSlider.java
UserLogin.java
UserRegistration.java
RestaurantLogin.java
RestaurantRegistration.java
AdminLogin.java
AdminDashboard.java
RestaurantDashboard.java
MainActivity.java
MapsActivity.java
Cart.java
RestaurantMenuActivity.java
Fragments
HomeFragment.java
SavedFragment.java
HistoryFragment.java
ProfileFragment.java
Adapters
RestaurantAdapter.java
RestaurantsAdapter.java
MenuAdapter.java
CartAdapter.java
OrderHistoryAdapter.java
UsersAdapter.java
ViewPagerAdapter.java
SliderAdapter.java
Supporting Classes
Restaurant.java
MenuItem.java
CartItem.java
OrderHistoryItem.java
ScreenItem.java
Prerequisites
Before running this project, ensure you have:

Android Studio installed
JDK 8 or compatible version
Android SDK with API 34 support
A Google Firebase project
A Google Cloud project with Maps and Places APIs enabled
Razorpay account for test payment configuration
Setup and Installation
1. Clone the Repository
bash
git clone https://github.com/Pronobesh-Coder/Cravify.git
cd Cravify
2. Open in Android Studio
Open Android Studio
Choose “Open an Existing Project”
Select the Cravify folder
3. Sync Gradle
Once the project is opened:

Let Gradle sync automatically
If prompted, install missing Android SDK components
Ensure all dependencies are resolved
Firebase Configuration
This project uses Firebase for:

User Authentication
Firestore database
Storage
Notifications
Required Steps
Create a Firebase project in the Firebase Console
Add an Android app with package name:
Text
com.example.cravify
Download the google-services.json file
Place it in:
Text
app/google-services.json
Enable:
Firebase Authentication
Email/Password sign-in
Firestore
Firebase Cloud Messaging
Firebase Storage
Firestore / Data Model
The app appears to use collections such as:

users
restaurants
The Java code accesses these collections to fetch user and restaurant records, so the Firebase database should be configured accordingly.

Note: The repository includes a google-services.json placeholder-style configuration in the project tree, but for a real app build you should replace it with your own Firebase-generated file.

Google Maps Configuration
The Android manifest includes Google Maps metadata:

XML
<meta-data
    android:name="com.google.android.geo.API_KEY"
    android:value="AIzaSyCE_uX0dmtE96aMiwjy9x0OF--qPQcsG14" />
This key is hardcoded in the project and should be replaced with your own API key for production use.

Required APIs
Enable the following in Google Cloud Console:

Maps SDK for Android
Places API
Geolocation-related services if required by the app
Permissions
The app requests the following permissions in AndroidManifest.xml:

XML
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
Android 13+ may also require:

POST_NOTIFICATIONS
The app handles runtime permission requests for location and notifications during login flow.

Build and Run
Debug Build
bash
./gradlew assembleDebug
Install on Connected Device
bash
./gradlew installDebug
Or run directly from Android Studio using an emulator or physical device.

Usage
For Customers
Create an account or sign in
Browse restaurants and menu items
Save favorite restaurants
Add items to the cart
Place orders
Track order history
For Restaurant Owners
Register as a restaurant partner
Login to dashboard
Manage restaurant info and menu
For Admins
Access admin login
Manage operational data and user/restaurant flows
Known Notes
The project is an educational/internship-style Android application.
The package name is com.example.cravify, which is a default example namespace and may be customized for production use.
Some API keys and tokens are hardcoded in source files; they should be moved to secure environment-based configuration for production deployment.
The app is designed primarily for Android mobile devices.
Some functionality may depend on external backend data and Firebase configuration being correctly set up.
License
This project is intended for educational and internship-related use. Please check with the author or repository owner before using it for commercial purposes.

Contributing
If you want to improve the app:

Fork the project
Create a feature branch
Commit your changes
Open a pull request
Acknowledgements
Firebase
Google Maps
Razorpay
Android Jetpack / Material Components
Open-source Android libraries used in this project

