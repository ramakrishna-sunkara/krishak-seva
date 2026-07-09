"""
asr.py — audio -> text
Pipeline: raw audio -> noise reduction -> 16kHz mono WAV -> Whisper transcription.
Uses Groq's cloud-hosted Whisper API for ultra-fast, highly accurate multilingual (Telugu/English) ASR.
Falls back to local whisper if Groq is unavailable.
"""

import os
import uuid
from groq import Groq
from dotenv import load_dotenv

load_dotenv()

_IS_VERCEL = os.environ.get("VERCEL") == "1"
_local_whisper_model = None
TEMP_DIR = os.path.join("/tmp", "temp_audio") if _IS_VERCEL else os.path.join(os.path.dirname(__file__), "temp_audio")
os.makedirs(TEMP_DIR, exist_ok=True)
_groq_key = os.environ.get("GROQ_API_KEY", "")
_client = Groq(api_key=_groq_key) if _groq_key else None


def _denoise_and_resample(input_path: str) -> str:
    if _IS_VERCEL:
        return input_path
    import librosa
    import soundfile as sf
    import noisereduce as nr
    y, sr = librosa.load(input_path, sr=None, mono=True)
    if len(y) > sr:
        noise_clip = y[: int(sr * 0.5)]
        reduced = nr.reduce_noise(y=y, sr=sr, y_noise=noise_clip, stationary=False)
    else:
        reduced = nr.reduce_noise(y=y, sr=sr, stationary=False)
    if sr != 16000:
        reduced = librosa.resample(reduced, orig_sr=sr, target_sr=16000)
    out_path = os.path.join(TEMP_DIR, f"{uuid.uuid4().hex}.wav")
    sf.write(out_path, reduced, 16000)
    return out_path

def transcribe_audio(input_path: str) -> dict:
    """
    Full ASR pipeline entry point.
    Attempts Groq Cloud Whisper Large V3 first (high speed & accuracy).
    Falls back to local Whisper base model.
    """
    global _local_whisper_model
    cleaned_path = None
    try:
        cleaned_path = _denoise_and_resample(input_path)
    except Exception as e:
        print("Denoise and resample failed, using raw audio or fallback:", e)
        cleaned_path = input_path

    transcript = ""
    language = "en"

    # Attempt Groq Cloud Whisper API (blazing fast, high accuracy)
    if _client:
        try:
            with open(cleaned_path, "rb") as file:
                transcription = _client.audio.transcriptions.create(
                    file=(os.path.basename(cleaned_path), file.read()),
                    model="whisper-large-v3",
                    response_format="json"
                )
                transcript = transcription.text.strip()
                language = "te" if any(ord(c) > 3000 for c in transcript) else "en"
                try:
                    safe_print = transcript.encode("ascii", "ignore").decode("ascii")
                    print(f"Groq Whisper ASR Transcript: {safe_print} (Language: {language})")
                except Exception:
                    pass
                
                if cleaned_path != input_path and os.path.exists(cleaned_path):
                    os.remove(cleaned_path)
                return {"transcript": transcript, "language": language}
        except Exception as e:
            print("Groq Whisper Cloud API failed, falling back to local model:", e)

    # Fallback to Local Whisper Model (not available on Vercel)
    if _IS_VERCEL:
        return {"transcript": transcript, "language": language}
    try:
        if _local_whisper_model is None:
            print("Loading local Whisper base model...")
            import whisper
            _local_whisper_model = whisper.load_model("base")
            
        result = _local_whisper_model.transcribe(cleaned_path, task="transcribe")
        transcript = result.get("text", "").strip()
        language = result.get("language", "en")
    except Exception as e:
        print("ASR failed completely:", e)
    finally:
        if cleaned_path != input_path and os.path.exists(cleaned_path):
            os.remove(cleaned_path)

    return {"transcript": transcript, "language": language}