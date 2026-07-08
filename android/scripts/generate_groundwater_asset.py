#!/usr/bin/env python3
"""Merge AP/TG groundwater CSVs into app asset JSON."""

import csv
import json
import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
DATA_DIR = ROOT / "data" / "groundwater"
ASSET_PATH = ROOT / "app" / "src" / "main" / "assets" / "groundwater" / "groundwater_districts.json"

DISTRICT_ALIASES = {
    ("Andhra Pradesh", "Anantapur"): "Ananthapuramu",
    ("Andhra Pradesh", "Ananthapuramu"): "Ananthapuramu",
    ("Andhra Pradesh", "Kadapa"): "Y.S.R Kadapa",
    ("Andhra Pradesh", "YSR Kadapa"): "Y.S.R Kadapa",
    ("Andhra Pradesh", "Y.S.R Kadapa"): "Y.S.R Kadapa",
    ("Andhra Pradesh", "Nellore"): "Sri Potti Sriramulu Nellore",
    ("Telangana", "Jayashankar Bhupalpally"): "Jayashankar Bhupalapally",
    ("Telangana", "Komaram Bheem"): "Komarambheem Asifabad",
    ("Telangana", "Medchal-Malkajgiri"): "Medchal Malkajgiri",
    ("Telangana", "Rajanna Sircilla"): "Rajanna Siricilla",
    ("Telangana", "Peddapalli"): "Peddapalle",
}


def categorize(stage: float) -> str:
    if stage > 100:
        return "OVER_EXPLOITED"
    if stage >= 90:
        return "CRITICAL"
    if stage >= 70:
        return "SEMI_CRITICAL"
    return "SAFE"


def parse_csv(path: Path, state: str) -> list[dict]:
    rows = []
    with path.open(newline="", encoding="utf-8-sig") as handle:
        reader = csv.DictReader(handle)
        for row in reader:
            district = (row.get("Name of District") or "").strip()
            if not district or district.lower().startswith("total"):
                continue
            stage = float((row.get("Stage of GW extraction (%)") or "").strip())
            aliases = {district}
            for (alias_state, alias), canonical in DISTRICT_ALIASES.items():
                if alias_state == state and canonical == district:
                    aliases.add(alias)
            rows.append(
                {
                    "state": state,
                    "district": district,
                    "stageOfExtractionPercent": round(stage, 2),
                    "category": categorize(stage),
                    "annualExtractableResourceHam": float(row["Annual Extractable Groundwater Resource"]),
                    "totalAnnualExtractionHam": float(row["Total Annual Extraction"]),
                    "netAvailabilityForFutureHam": float(row["Net GW availability for future"]),
                    "totalAnnualRechargeHam": float(row["Total annual groundwater recharge"]),
                    "assessmentYear": "2022",
                    "source": "CGWB National Compilation via OpenCity CSV",
                    "districtAliases": sorted(aliases),
                }
            )
    return rows


def main() -> int:
    ap_path = DATA_DIR / "andhra_pradesh_groundwater.csv"
    tg_path = DATA_DIR / "telangana_groundwater.csv"
    if not ap_path.exists() or not tg_path.exists():
        missing = [str(path) for path in (ap_path, tg_path) if not path.exists()]
        print("Missing CSV files:", file=sys.stderr)
        for path in missing:
            print(f"  - {path}", file=sys.stderr)
        return 1
    records = parse_csv(ap_path, "Andhra Pradesh") + parse_csv(tg_path, "Telangana")
    ASSET_PATH.parent.mkdir(parents=True, exist_ok=True)
    ASSET_PATH.write_text(json.dumps(records, indent=2, ensure_ascii=False), encoding="utf-8")
    print(f"Wrote {len(records)} records to {ASSET_PATH}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
