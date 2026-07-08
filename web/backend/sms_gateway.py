"""
sms_gateway.py — SMS and outbound call alerts via Twilio (optional in demo mode).
"""

import os
from dotenv import load_dotenv

load_dotenv()

_ACCOUNT_SID = os.environ.get("TWILIO_ACCOUNT_SID", "")
_AUTH_TOKEN = os.environ.get("TWILIO_AUTH_TOKEN", "")
_FROM_NUMBER = os.environ.get("TWILIO_PHONE_NUMBER", "")
_client = None

if _ACCOUNT_SID and _AUTH_TOKEN:
    try:
        from twilio.rest import Client
        _client = Client(_ACCOUNT_SID, _AUTH_TOKEN)
    except Exception as exc:
        print("Twilio client init failed:", exc)


def send_sms(to_number: str, message: str) -> dict:
    if not _client or not _FROM_NUMBER:
        print(f"[SMS DEMO] To {to_number}: {message[:120]}...")
        return {"status": "demo", "to": to_number, "body": message}
    try:
        sms = _client.messages.create(body=message, from_=_FROM_NUMBER, to=to_number)
        return {"sid": sms.sid, "status": sms.status, "to": to_number, "body": message}
    except Exception as exc:
        print("Twilio SMS send failed:", exc)
        return {"error": str(exc), "status": "failed", "to": to_number, "body": message}


def make_voice_call_alert(to_number: str, message_text: str) -> dict:
    if not _client or not _FROM_NUMBER:
        print(f"[CALL DEMO] To {to_number}: voice alert skipped (Twilio not configured).")
        return {"status": "demo", "to": to_number}
    try:
        from twilio.twiml.voice_response import VoiceResponse
        response = VoiceResponse()
        response.say(message_text, voice="alice", language="en-IN")
        call = _client.calls.create(twiml=str(response), from_=_FROM_NUMBER, to=to_number)
        return {"call_sid": call.sid, "status": call.status, "to": to_number}
    except Exception as exc:
        print("Twilio outbound call failed:", exc)
        return {"error": str(exc), "status": "failed", "to": to_number}


def dispatch_severe_weather_alert(phone: str, crop: str, location: str, weather_data: dict) -> dict:
    alerts = weather_data.get("alerts", [])
    if not alerts:
        return {"status": "no_alerts", "message": "No severe conditions detected."}
    alert = alerts[0]
    alert_type = alert["type"]
    alert_msg = alert["message"]
    lang = "te" if any(0x0C00 <= ord(c) <= 0x0C7F for c in alert_msg) else "en"
    remedy_hint = "Monitor crop status closely and consult extension officers."
    if alert_type == "HEAVY_RAIN":
        remedy_hint = "Delay fertilizers and clear drainage channels."
    elif alert_type == "EXTREME_HEAT":
        remedy_hint = "Increase early-morning watering cycles."
    elif alert_type == "DRY_SPELL":
        remedy_hint = "Use drip irrigation to conserve water."
    title_msg = f"KṛṣakaSevā alert for {crop or 'crop'} at {location or 'farm'}:\n"
    full_message = f"{title_msg}Warning: {alert_msg}\nAdvice: {remedy_hint}"
    sms_res = send_sms(phone, full_message)
    call_res = make_voice_call_alert(phone, full_message)
    return {"status": "dispatched", "alert_type": alert_type, "phone": phone, "sms_result": sms_res, "call_result": call_res}
