"""AKAI 답변 정확도 자동 채점 (WBS 5.2, NF-01).

questions.csv의 질문을 실행 중인 서버(POST /api/questions)에 보내고 세 지표를 계산한다.
  - 정답률: 답 있음 질문 중 found=true이고 정답 핵심어가 답변에 모두 들어 있는 비율
  - 근거 표시율: found=true인 답변 중 근거(문서명·쪽·원문)가 1개 이상 함께 온 비율
  - 거절률: 답 없음 질문 중 "문서에서 찾을 수 없음"(found=false)으로 답한 비율
참고로 근거 일치율(정답 근거 문서·쪽이 근거 목록에 있는 비율)과 응답 시간(NF-02)도 함께 낸다.

정답 핵심어 칸: 세미콜론(;)은 "모두 포함", 세로줄(|)은 "그중 하나". 공백·쉼표는 무시하고 비교한다.

실행 (서버를 켜 둔 상태에서, 표준 라이브러리만 사용):
  python eval/evaluate.py --label embedding
  python eval/evaluate.py --questions questions_paraphrase.csv --label embedding-paraphrase
  python eval/evaluate.py --check        # 채점 규칙 자체 확인만
결과는 eval/results/<날짜시각>-<label>.csv 로 저장한다. 질문은 이력에 남는다(로그인 계정 기준).
"""

import argparse
import csv
import http.cookiejar
import json
import re
import sys
import time
import urllib.error
import urllib.request
from datetime import datetime
from pathlib import Path

EVAL_DIR = Path(__file__).resolve().parent
ANSWERABLE = "답 있음"


def normalize(text):
    return re.sub(r"[\s,]+", "", text or "").lower()


def has_keywords(answer, keywords):
    """keywords 예: '130학점', '4분의3|75%', 'A;B'"""
    if not keywords.strip():
        return False
    body = normalize(answer)
    return all(
        any(normalize(option) in body for option in group.split("|") if option.strip())
        for group in keywords.split(";")
    )


def grade(row, response):
    """질문 한 개의 채점 결과"""
    found = bool(response.get("found"))
    evidences = response.get("evidences") or []
    answerable = row["유형"] == ANSWERABLE
    evidence_match = any(
        e.get("documentTitle") == row["근거 문서"] and str(e.get("pageNo")) == row["근거 쪽"] for e in evidences
    )
    return {
        "found": found,
        "evidence_count": len(evidences),
        "correct": answerable and found and has_keywords(response.get("answer", ""), row["정답 핵심어"]),
        "evidence_shown": found and len(evidences) > 0,
        "evidence_match": answerable and evidence_match,
        "rejected": (not answerable) and not found,
    }


def percent(part, total):
    return round(part * 100 / total, 1) if total else None


def summarize(rows):
    answerable = [r for r in rows if r["유형"] == ANSWERABLE]
    unanswerable = [r for r in rows if r["유형"] != ANSWERABLE]
    found = [r for r in rows if r["found"]]
    times = [r["seconds"] for r in rows if r["seconds"] is not None]
    return {
        "질문 수": len(rows),
        "정답률(%)": percent(sum(r["correct"] for r in answerable), len(answerable)),
        "근거 표시율(%)": percent(sum(r["evidence_shown"] for r in found), len(found)),
        "거절률(%)": percent(sum(r["rejected"] for r in unanswerable), len(unanswerable)),
        "근거 일치율(%)": percent(sum(r["evidence_match"] for r in answerable), len(answerable)),
        "평균 응답(초)": round(sum(times) / len(times), 2) if times else None,
        "최대 응답(초)": round(max(times), 2) if times else None,
        "5초 이내(%)": percent(sum(t <= 5 for t in times), len(times)),
        "오류 수": sum(1 for r in rows if r["error"]),
    }


def post_json(opener, url, payload):
    request = urllib.request.Request(
        url, data=json.dumps(payload).encode("utf-8"), headers={"Content-Type": "application/json"}, method="POST"
    )
    with opener.open(request, timeout=60) as response:
        return json.loads(response.read().decode("utf-8"))


def run(args):
    with open(EVAL_DIR / args.questions, encoding="utf-8-sig", newline="") as f:
        questions = list(csv.DictReader(f))

    opener = urllib.request.build_opener(urllib.request.HTTPCookieProcessor(http.cookiejar.CookieJar()))
    post_json(opener, args.base + "/api/auth/login", {"loginId": args.user, "password": args.password})

    rows = []
    for q in questions:
        start = time.perf_counter()
        error, response = "", {}
        try:
            response = post_json(opener, args.base + "/api/questions", {"question": q["질문"]})
        except urllib.error.HTTPError as e:
            error = f"HTTP {e.code}"
        seconds = round(time.perf_counter() - start, 2)
        result = {**q, **grade(q, response), "seconds": None if error else seconds, "error": error}
        result["answer"] = response.get("answer", "")
        result["evidences"] = " / ".join(
            f"{e.get('documentTitle')} {e.get('pageNo')}쪽" for e in response.get("evidences") or []
        )
        rows.append(result)
        mark = "O" if (result["correct"] or result["rejected"]) else "X"
        print(f"{q['번호']} {mark} found={result['found']} {seconds}s  {result['answer'][:60]}")

    out_dir = EVAL_DIR / "results"
    out_dir.mkdir(exist_ok=True)
    out = out_dir / f"{datetime.now():%Y%m%d-%H%M}-{args.label}.csv"
    columns = ["번호", "질문", "유형", "정답 핵심어", "근거 문서", "근거 쪽", "found", "correct", "rejected",
               "evidence_shown", "evidence_match", "evidence_count", "seconds", "error", "answer", "evidences"]
    with open(out, "w", encoding="utf-8-sig", newline="") as f:
        writer = csv.DictWriter(f, fieldnames=columns, extrasaction="ignore")
        writer.writeheader()
        writer.writerows(rows)

    summary = summarize(rows)
    print(f"\n[{args.label}] 결과 저장: {out}")
    for key, value in summary.items():
        print(f"  {key}: {value}")
    return summary


def self_check():
    assert has_keywords("졸업하려면 총 130 학점이 필요합니다.", "130학점")
    assert has_keywords("수업 시간의 75% 이상", "4분의3|75%")
    assert not has_keywords("수업 시간의 절반", "4분의3|75%")
    assert has_keywords("A와 B", "a;b") and not has_keywords("A만", "a;b")
    assert not has_keywords("아무 답", "")
    row = {"유형": ANSWERABLE, "정답 핵심어": "90일", "근거 문서": "04_x", "근거 쪽": "1"}
    g = grade(row, {"found": True, "answer": "90 일마다", "evidences": [{"documentTitle": "04_x", "pageNo": 1}]})
    assert g["correct"] and g["evidence_shown"] and g["evidence_match"] and not g["rejected"]
    g = grade({**row, "유형": "답 없음"}, {"found": False, "answer": "문서에서 찾을 수 없음", "evidences": []})
    assert g["rejected"] and not g["correct"]
    s = summarize([
        {"유형": ANSWERABLE, "found": True, "correct": True, "evidence_shown": True, "evidence_match": True,
         "rejected": False, "seconds": 1.0, "error": ""},
        {"유형": "답 없음", "found": True, "correct": False, "evidence_shown": False, "evidence_match": False,
         "rejected": False, "seconds": 6.0, "error": ""},
    ])
    assert s["정답률(%)"] == 100.0 and s["근거 표시율(%)"] == 50.0 and s["거절률(%)"] == 0.0 and s["5초 이내(%)"] == 50.0
    print("채점 규칙 확인 통과")


if __name__ == "__main__":
    sys.stdout.reconfigure(encoding="utf-8")  # Windows 콘솔에서도 한글 출력
    parser = argparse.ArgumentParser(description="AKAI 답변 정확도 자동 채점")
    parser.add_argument("--base", default="http://localhost:8080")
    parser.add_argument("--user", default="member01")
    parser.add_argument("--password", default="akai1234", help="샘플 계정 비밀번호(application.properties)")
    parser.add_argument("--questions", default="questions.csv", help="eval 폴더 안의 질문 파일")
    parser.add_argument("--label", default="run", help="결과 파일 이름에 붙일 이름 (예: embedding, keyword)")
    parser.add_argument("--check", action="store_true", help="서버 없이 채점 규칙만 확인")
    parsed = parser.parse_args()
    if parsed.check:
        self_check()
        sys.exit(0)
    run(parsed)
