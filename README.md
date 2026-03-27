# Cyber Maze Escape — Minigame + CTF Walkthrough

This repo contains **Cyber Maze Escape**, a small cyberpunk maze game built on a custom abstract engine (input, movement, collision, ECS) with LibGDX-backed renderers on desktop.

This README is a **full walkthrough** for all **6 terminal challenges** (5 “minigames” + the terminal-based CTF challenges).

## How to run

- **Desktop (LWJGL3)**:

```bash
./gradlew.bat lwjgl3:run
```

## Controls (default)

- **W/A/S/D**: move
- **E**: interact (pick up intel / use terminal)
- **H**: signal ping (reveals nearby intel objects)
- **ESC**: settings / back
- **SPACE**: confirm / continue on menus

## Challenge list (what counts as the “6”)

The terminal nodes in the levels are wired from:
- `core/src/main/java/io/github/INF1009_P10_Team7/cyber/level/Level1Config.java`
- `core/src/main/java/io/github/INF1009_P10_Team7/cyber/level/Level2Config.java`

Challenges are implementations of `IMiniGame`:
- **Binary Decoder** (`BinaryDecodeGame`)
- **Caesar Cipher** (`CaesarCipherGame`)
- **Port Mapper** (`PortMatchGame`)
- **Doc Analysis** (`LogAnalysisGame`)
- **Packet Sniffer** (`PacketSnifferGame`) *(Level 2)*
- **CTF Terminal** (`TerminalMiniGame`) → wraps an `ICTFChallenge`

---

## 1) Binary Decoder — “INTERCEPTED SIGNAL”

### Goal
Decode each 8-bit binary byte into an ASCII letter and type the final word **one character at a time** (ENTER after each).

### How to solve
- Use an ASCII table (the game even hints this).
- Each byte corresponds to one uppercase letter.
- Type the correct **single character** for the current byte and press **ENTER**.

### Answers by level
- **Level 1**: the decoded word is **`FLAGS`** (configured as `new BinaryDecodeGame("FLAGS")`)
- **Level 2**: the decoded word is **`CRYPT`** (configured as `new BinaryDecodeGame("CRYPT")`)

---

## 2) Caesar Cipher — “CAESAR CIPHER DECODER”

### Goal
Rotate the alphabet until the decoded output becomes **HELLO HACKER**, then submit.

### Inputs
- **A / LEFT**: rotate left
- **D / RIGHT**: rotate right
- **ENTER**: submit

### Solution
- This challenge is **ROT-13**.
- Set **SHIFT = 13**, then press **ENTER**.

---

## 3) Port Mapper — “NETWORK CONFIG // PORT MAPPER”

### Goal
Match 5 ports (1–5) to the correct service (A–E).

### Inputs
- Press **1–5** to pick a port row
- Then press **A–E** to choose the matching service

### Level 1 solution (well-known ports)
Ports are: `22`, `80`, `443`, `3306`, `21`  
Services shown are: `A MySQL`, `B FTP`, `C SSH`, `D HTTPS`, `E HTTP`

- **1 (22)** → **C (SSH)**
- **2 (80)** → **E (HTTP)**
- **3 (443)** → **D (HTTPS)**
- **4 (3306)** → **A (MySQL)**
- **5 (21)** → **B (FTP)**

### Level 2 solution (less common services)
Ports are: `25`, `110`, `143`, `3389`, `5432`  
Services shown are: `A PostgreSQL`, `B IMAP`, `C RDP`, `D SMTP`, `E POP3`

- **1 (25)** → **D (SMTP)**
- **2 (110)** → **E (POP3)**
- **3 (143)** → **B (IMAP)**
- **4 (3389)** → **C (RDP)**
- **5 (5432)** → **A (PostgreSQL)**

---

## 4) Doc Analysis — “DOC FORENSICS // INTERCEPTED MAIL DUMP”

### Goal
Read the intercepted email dump, find the override code, and type the correct plaintext into the prompt (ENTER to submit).

### Inputs
- **UP/DOWN**: scroll line-by-line
- **PgUp/PgDn**: scroll faster
- Type in the answer and press **ENTER**

### Level 1 solution (plaintext)
The email literally contains the override code:
- **Answer**: `override_7734_alpha`

### Level 2 solution (Atbash “mirror encoding”)
The email shows an encoded override:
- Ciphertext shown in email: `ZXXVHH_9182_WVOGZ`
- Decode using **Atbash** (A↔Z, B↔Y, ...), numbers/underscores stay the same.
- **Answer**: `access_9182_delta`

---

## 5) Packet Sniffer — “PACKET SNIFFER” (Level 2)

### Goal
Identify the protocol name from the hex dump + ASCII decode and type it (ENTER to submit).

### Inputs
- Type one of: `HTTP`, `DNS`, `SSH`, `FTP`, `SMTP`
- Press **ENTER**

### How to solve each packet type (what to look for)
- **HTTP**
  - ASCII shows: `GET /index.html HTTP/1.1` and `Host:`
- **DNS**
  - “Port 53” style header bytes, and the domain name appears in the query (e.g. `example.com`)
- **SSH**
  - ASCII banner: `SSH-2.0-OpenSSH_...`
- **FTP**
  - Greeting like: `220 (vsFTPd ...)` plus `USER` / `PASS`
- **SMTP**
  - Greeting like: `220 mail.server.com` plus `EHLO` and `250-` responses

*(The game randomly selects one of the 5 protocol captures each time.)*

---

## 6) CTF Terminal Challenges (“TerminalMiniGame”)

These are the “CTF thingies” where you type shell-like commands in a terminal emulator.

### 6.1 TERMINAL-01 // NETWORK RECON (Level 1)

**Objective**: find the hidden flag on `192.168.10.105`

Walkthrough:
1. Scan the subnet:
   - `nmap 192.168.10.0/24`
2. Connect:
   - `connect 192.168.10.105` *(or `ssh root@192.168.10.105`)*
3. List files:
   - `ls`
4. Read the hint:
   - `cat notes.txt`
5. Show hidden directories:
   - `ls -la`
6. Enter the hidden directory:
   - `cd .secret`
7. Read the flag:
   - `cat flag.txt`

Flag shown:
- `FLAG{r3c0n_m4st3r_1337}`

### 6.2 TERMINAL-03 // WEB SQL INJECTION (Level 2)

**Objective**: dump DB creds, authenticate, then read `/var/www/flag.txt`

Walkthrough:
1. Dump credentials:
   - `sqlmap -u "http://10.10.10.50/login?id=1" -D appdb -T users --dump`
2. Log in:
   - `login admin S3cr3t!Flag`
3. Read the flag:
   - `cat /var/www/flag.txt`

Flag shown:
- `FLAG{sqli_d4t4b4s3_pwn3d}`

---

## Notes for graders / reviewers

- The exact minigame configuration (words, documents, port lists) is declared in `Level1Config` and `Level2Config`.
- CTF terminal challenges are implementations of `ICTFChallenge` wrapped by `TerminalMiniGame`.
