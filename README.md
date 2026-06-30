# Life Inbox

Life Inbox turns raw life information into confirmable candidates.

The first version keeps the core product boundary explicit:

- `InboxEntry` stores the original input.
- `LifeItemCandidate` stores model or extractor suggestions with evidence.
- `LifeItem` stores user-confirmed items.

The service intentionally does not auto-confirm extracted facts. Candidates must be confirmed or ignored.

## Run

```bash
mkdir -p out
javac -d out $(find src/main/java -name '*.java')
java -cp out com.lifeinbox.LifeInboxServer
```

The server listens on `http://localhost:8080` by default.

## API

Submit and parse:

```bash
curl -s -X POST http://localhost:8080/api/inbox/entries \
  -H 'Content-Type: application/json' \
  -d '{"content":"Your hotel reservation is confirmed for July 18-21. Free cancellation is available until July 15."}'
```

Confirm a candidate:

```bash
curl -s -X POST http://localhost:8080/api/inbox/entries/{entryId}/candidates/{candidateId}/confirm \
  -H 'Content-Type: application/json' \
  -d '{"title":"Hotel stay"}'
```

Ignore a candidate:

```bash
curl -s -X POST http://localhost:8080/api/inbox/entries/{entryId}/candidates/{candidateId}/ignore
```

## Notes

This repository currently uses a conservative rule-based extractor so the HTTP and domain flow can be tested without a network dependency. The `llm` package already defines the provider/extractor boundary for replacing it with a real model-backed implementation.

Dates without an explicit year are treated as missing information instead of being guessed.
