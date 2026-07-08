"""
farmer_profile.py — simple farmer profiling ("Krishi Sakhi" style)
Day 1/2 version: local JSON file storage. Swap for a real DB (SQLite/Postgres)
if the team has time later.
"""

import os
import json

PROFILE_PATH = os.path.join(os.path.dirname(__file__), "farmer_profiles.json")


def _load_all() -> dict:
    if not os.path.exists(PROFILE_PATH):
        return {}
    with open(PROFILE_PATH, "r", encoding="utf-8") as f:
        return json.load(f)


def _save_all(data: dict):
    with open(PROFILE_PATH, "w", encoding="utf-8") as f:
        json.dump(data, f, indent=2)


def create_or_update_profile(phone: str, name: str = "", location: str = "",
                              land_size_acres: float = None, crop_type: str = "",
                              soil_type: str = "", irrigation_method: str = "",
                              water_availability: str = "", soil_ph: float = None,
                              latitude: float = None, longitude: float = None) -> dict:
    profiles = _load_all()
    profile = profiles.get(phone, {})
    profile.update({
        "phone": phone,
        "name": name or profile.get("name", ""),
        "location": location or profile.get("location", ""),
        "land_size_acres": land_size_acres if land_size_acres is not None else profile.get("land_size_acres"),
        "crop_type": crop_type or profile.get("crop_type", ""),
        "soil_type": soil_type or profile.get("soil_type", ""),
        "irrigation_method": irrigation_method or profile.get("irrigation_method", ""),
        "water_availability": water_availability or profile.get("water_availability", ""),
        "soil_ph": soil_ph if soil_ph is not None else profile.get("soil_ph"),
        "latitude": latitude if latitude is not None else profile.get("latitude"),
        "longitude": longitude if longitude is not None else profile.get("longitude"),
    })
    profiles[phone] = profile
    _save_all(profiles)
    return profile


def get_profile(phone: str) -> dict:
    profiles = _load_all()
    return profiles.get(phone, {})


def get_latest_phone_number() -> str:
    """Returns the phone number of the latest profile added to the database."""
    profiles = _load_all()
    if not profiles:
        return ""
    return list(profiles.keys())[-1]