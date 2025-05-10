# ShadowTaxi 🚖 — Java Arcade Game

This repository contains my implementation of **ShadowTaxi**, a scrolling taxi simulation game developed using Java and the **Bagel game engine**. This project was completed for [SWEN20003: Object Oriented Software Development] at the University of Melbourne.

## 🎮 Game Overview

In ShadowTaxi, you play as a driver trying to survive on an endless road:

- Pick up and drop off passengers at their designated flags
- Earn money based on distance and passenger priority
- Avoid collisions with cars and enemy vehicles
- Collect coins and invincibility power-ups
- Beat a target score of **500** before time runs out!

## 🧱 Key Features

- **Full OOP Design** using Java
- Modular structure with entities: `Taxi`, `Driver`, `Passenger`, `EnemyCar`, `Coin`, `Trip`, etc.
- **Health system** for taxi, driver, and passengers
- **Scrolling background** with weather conditions (sunny/rain)
- Custom file parsing (`gameObjects.csv`, `gameWeather.csv`)
- Interactive UI: Home, Player Info, Gameplay, End Screens

## 🚀 How to Run

```bash
# Using Maven:
mvn compile
mvn exec:java -Dexec.mainClass=ShadowTaxi
