<div align="center">

#  Safety Compliance Agent

### AI-Powered Safety Compliance & Incident Management for Manufacturing
Autonomous · RAG-Grounded · Multi-Tenant · Real-Time Alerting

![Java](https://img.shields.io/badge/Java-21-orange) ![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5-brightgreen) ![Gemini](https://img.shields.io/badge/LLM-Gemini-blue) ![MySQL](https://img.shields.io/badge/Database-MySQL-4479A1) ![License](https://img.shields.io/badge/License-Academic-lightgrey)

Safety Compliance Agent is a full-stack, AI-powered platform that transforms manufacturing safety management from a manual, paper-driven process into an autonomous, consistent, and instantly responsive system — combining Retrieval-Augmented Generation, automatic incident classification, and real-time alerting, all served through a single Spring Boot application.

📖 Overview · 🎯 Features · 🏗️ Architecture · 💬 Walkthrough · 🚀 Quick Start

</div>

## 📑 Table of Contents

| Core Sections | Technical Deep Dives | Resources |
|---|---|---|
| 🎯 Key Features | 🏛️ Three-Layer Intelligence Stack | 🛠️ Tech Stack |
| 🆚 Why This Approach? | 🗂️ RAG Ingestion Pipeline | ⚙️ Configuration |
| 🏗️ System Architecture | ⚖️ Incident Classification Engine | 🗺️ Roadmap |
| 💬 Request Walkthrough | 🔐 Multi-Tenant Design | 👩‍💻 Author |
| 🚀 Quick Start | 📲 Alerting Pipeline | 📁 Project Structure |

## 🎯 Key Features

| Feature | Description |
|---|---|
| 🤖 AI Chat Assistant | Retrieval-Augmented Generation grounded strictly in each company's uploaded safety documents |
| 📝 Smart Incident Classification | LLM few-shot prompting auto-assigns severity, category & recommended action |
| 🎙️ Bilingual Voice Input | Tamil (`ta-IN`) and English (`en-IN`) speech-to-text for hands-free reporting |
| 🦺 Machine-Aware PPE Checklist | Dynamic safety gear checklist per selected machine before submission |
| 📲 Real-Time Alerting | Automatic SMS (Twilio/Fast2SMS) + Email (Gmail SMTP) on high-severity incidents |
| 🏢 Multi-Tenant Isolation | Company-scoped documents, incidents & machines — zero data leakage across tenants |
| 📊 Live Analytics Dashboard | Severity/category breakdowns + machine-wise risk ranking via Chart.js |
| 📄 Self-Indexing Documents | Upload → auto-chunk → embed → index, ready for retrieval within seconds |

## 🆚 Why This Approach?

| Capability | Traditional Safety Process | Safety Compliance Agent |
|---|---|---|
| Answering safety questions | Ask a supervisor, wait | ✨ Instant, AI-grounded answers |
| Policy source | Paper manuals / scattered PDFs | 📚 Live RAG from company documents |
| Severity classification | Subjective, supervisor-dependent | 🧠 Consistent LLM-based classification |
| Alerting | Manual phone calls | 📲 Automatic SMS + Email on high severity |
| Reporting method | Handwritten forms only | 🎙️ Type or speak — Tamil or English |
| Multi-company support | Separate spreadsheets per site | 🏢 Single platform, isolated tenant data |
| Analytics | Manual Excel counting | 📊 Real-time dashboard with risk ranking |


## 🏗️ System Architecture

```
┌──────────────────────────────────────────────────────────┐
│              🌐 FRONTEND (HTML / CSS / JS)                │
│   Login → Documents → Chat → Incident → Dashboard         │
└───────────────────────────┬────────────────────────────────┘
                            │  REST API (JSON)
┌───────────────────────────▼────────────────────────────────┐
│                 ☕ SPRING BOOT BACKEND                     │
│   Controllers → Services → Repositories (Spring Data JPA)  │
└───────┬───────────────────┬───────────────────┬────────────┘
        │                   │                   │
┌───────▼────────┐  ┌───────▼─────────┐ ┌───────▼──────────┐
│   MySQL/PostgreSQL│  Gemini API      │ │     Gmail        │
│  (structured data  │ (chat + embed)  │ │     ( Emai)      │
│  + JSON embeddings)│                 │ │                  │
└────────────────────┘  └──────────────┘  └──────────────────┘
```

## 🏛️ Three-Layer Intelligence Stack
┌──────────────────────────────────────────────────────────────┐
│                📱 WORKER INPUT (Text or Voice)               │
│         "What should I wear near the CNC machine?"            │
└──────────────────────────────┬────────────────────────────────┘
│
╔══════════════════════▼═══════════════════════════════╗
║   🎙️  LAYER 1 · INPUT NORMALIZATION                   ║
║   Web Speech API (Tamil/English) → Text               ║
║   ✓ Language detection  ✓ Transcript cleanup          ║
╚══════════════════════╤═══════════════════════════════╝
│
╔══════════════════════▼═══════════════════════════════╗
║   📚  LAYER 2 · KNOWLEDGE RETRIEVAL (RAG)            ║
║   Embed Query → Cosine Similarity → Company Filter    ║
║                                                       ║
║   Question Embedding ─────► gemini-embedding-001      ║
║   Vector Store ───────────► MySQL (JSON embeddings)   ║
║   Retrieval ───────────────► Top-3 relevant chunks    ║
╚══════════════════════╤═══════════════════════════════╝
│
╔══════════════════════▼═══════════════════════════════╗
║   ⚖️  LAYER 3 · GROUNDED GENERATION                  ║
║   Gemini (gemini-flash-latest)                        ║
║                                                       ║
║   📋 Context Inputs:                                  ║
║   ├─ Retrieved document chunks (company-specific)     ║
║   ├─ Strict grounding instruction (no hallucination)  ║
║   └─ Fallback: "I don't have that information"        ║
║                                                       ║
║   ✅ Output: Answer + Cited Source                    ║
╚════════════════════════════════════════════════════════╝
## 🗂️ RAG Ingestion Pipeline
Admin Uploads Document
│
▼
┌────────────────────────────────────────────┐
│  STEP 1 · SAVE METADATA                    │
│  Title, company, status = PROCESSING       │
└─────────────────────┬───────────────────────┘
│
▼
┌────────────────────────────────────────────┐
│  STEP 2 · CHUNKING                         │
│  Paragraph-based split (fallback: sentence)│
└─────────────────────┬───────────────────────┘
│
▼
┌────────────────────────────────────────────┐
│  STEP 3 · EMBEDDING                        │
│  gemini-embedding-001 (3072-dim vectors)   │
└─────────────────────┬───────────────────────┘
│
▼
┌────────────────────────────────────────────┐
│  STEP 4 · STORE + INDEX                    │
│  Chunks + embeddings → MySQL (JSON)        │
│  status → INDEXED                          │
└────────────────────────────────────────────┘
## ⚖️ Incident Classification Engine
╔══════════════════════════════════════════╗
║   INCIDENT DESCRIPTION (Text/Voice)      ║
╠══════════════════════════════════════════╣
║   Few-Shot Prompt with 2 worked examples ║
╠══════════════════════════════════════════╣
║   Gemini (gemini-flash-latest)           ║
╚═══════════════╤════════════════════════════╝
▼
┌───────────────────────────────────────────┐
│  Structured JSON Output                   │
│  { "severity": "high",                    │
│    "category": "electrical",              │
│    "recommendedAction": "..." }           │
└───────────────────┬─────────────────────────┘
▼
┌─────────────┴─────────────┐
▼                           ▼
Save to Database         severity == "high"?
│
┌──────────┴
▼ 
Email
| Severity | Trigger Condition | Action |
|---|---|---|
| 🟢 Low | Minor, no immediate risk | Logged, reviewed periodically |
| 🟡 Medium | Moderate risk, needs attention | Logged, flagged in dashboard |
| 🔴 High | Immediate danger to worker safety | SMS + Email sent instantly |

## 🔐 Multi-Tenant Design

Every core entity — `Machine`, `SafetyDocument`, `DocumentChunk`, `Incident` — carries a `companyName` field. Every query, upload, chat request, and incident report is scoped to the logged-in user's company, ensuring:

- Zero cross-company data visibility
- Independent safety document knowledge bases per company
- Isolated incident history and analytics per tenant

## 🚀 Quick Start

**1 · Clone & configure**

```bash
git clone https://github.com/Nive2501-engg/safety-compliance-agent.git
cd safety-compliance-agent
```

**2 · Set environment variables** (or edit `application.properties`)

```properties
DB_URL=jdbc:mysql://localhost:3306/safety_compliance_db
DB_USERNAME=root
DB_PASSWORD=your_password
GEMINI_API_KEY=your_gemini_api_key
ANTHROPIC_API_KEY=your_anthropic_api_key
```

**3 · Build & run**

```bash
mvn clean install
mvn spring-boot:run
```

**4 · Access the app** https://safety-compliance-agent.onrender.com

## 🗺️ Roadmap

| Stage | Status | Description |
|---|---|---|
| 1 | ✅ Done | Spring Boot backend, MySQL schema, CRUD APIs |
| 2 | ✅ Done | Gemini integration, embeddings, RAG pipeline |
| 3 | ✅ Done | Incident classification, SMS/Email alerting |
| 4 | ✅ Done | Multi-tenant login, PPE checklist, bilingual voice input |
| 5 | ✅ Done | Dashboard analytics, document delete, toast notifications |
| 6 | 🔲 Planned | Full Tamil/English UI translation, role-based access control |
| 7 | 🔲 Planned | Production deployment on AWS RDS |

## 👩‍💻 Author

**Nivetha S**
B.Tech Information Technology, V.S.B Engineering College
[GitHub](https://github.com/Nive2501-engg)
