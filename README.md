# Life Inbox

Life Inbox turns raw life information into confirmable candidates.
把生活中散落在邮件、截图、短信、网页和聊天记录里的信息，整理成可执行事项。

## **背景**
日常生活中常常不是缺少信息，而是信息散落在不同的地方、看过后就忘、不确定是否有用或者关联
下一步的行动，后续再想起来时，要么是忘记了，要么是找不到。

另外一个问题就是，多数信息都是非结构化的，需要进一步加工提取出可能的关键信息并记录下来。
这里采用LLM的协助，理解非结构化信息，并转换为结构化信息存储起来。

### 项目第一版
The first version keeps the core product boundary explicit:
项目第一版先完成产品的核心边界和功能。

- `InboxEntry` stores the original input，即用户的原始非结构化输入，第一版仅支持文本，后续再扩展
- `LifeItemCandidate` stores model or extractor suggestions with evidence，
    即大模型提取出的结构化信息
- `LifeItem` stores user-confirmed items，即用户确认后需要存储的最终结构化信息

#### User Interaction 用户交互
The service intentionally does not auto-confirm extracted facts. Candidates must be confirmed or ignored.
产品不会自动存储大模型提取的结构化信息，需要用户手动确认或者忽略。

第一版暂时没有实现前端页面，需要在窗口上手动执行命令， 以下是项目运行的命令列表：
## Run

```bash
mkdir -p out # 创建编译或者运行输入目录
javac -d out $(find src/main/java -name '*.java') # 编译
java -cp out com.lifeinbox.LifeInboxServer # 运行
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
