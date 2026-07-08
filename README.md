# Krishak Seva (कृषकसेवा)

Two independent projects in one repository.

**Repository:** [github.com/ramakrishna-sunkara/krishak-seva](https://github.com/ramakrishna-sunkara/krishak-seva)

| Project | Folder | Description |
|---------|--------|-------------|
| **Android** | [`android/`](android/) | Mobile farming assistant (Kotlin, Jetpack Compose, Firebase) |
| **Web** | [`web/`](web/) | Full-stack web platform (Flask + vanilla JS) |
| **Submission page** | [`docs/`](docs/) | **Hackathon landing page** — demos, links, docs (GitHub Pages) |

## Hackathon submission page

Open or host **`docs/index.html`** — one page with Android + Web details, demo videos, code links, and documentation.

```bash
open docs/index.html
```

**GitHub Pages:** Settings → Pages → Branch `main` → Folder **`/docs`** → Save

Live URL: `https://ramakrishna-sunkara.github.io/krishak-seva/`

## Android

```bash
cd android
./gradlew assembleDebug
```

See [android/README.md](android/README.md).

## Web

```bash
cd web/backend
python3 -m venv .venv && source .venv/bin/activate
pip install -r requirements.txt
python app.py
```

Deploy to **Google Cloud Run**: see [web/README.md](web/README.md).

