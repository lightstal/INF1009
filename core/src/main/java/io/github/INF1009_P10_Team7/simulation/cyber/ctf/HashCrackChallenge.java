package io.github.INF1009_P10_Team7.simulation.cyber.ctf;

/**
 * CTF Challenge 2 — Hash Cracking + Privilege Escalation.
 *
 * Flow:
 *   ls                          → shadow.bak, readme.txt
 *   cat readme.txt              → hint: shadow file leaked
 *   cat shadow.bak              → shows root hash
 *   hashcat --help              → shows usage
 *   hashcat -m 1800 <hash>      → cracking begins → password: "toor2024"
 *   su root                     → asks for password
 *   su root toor2024  OR
 *   password: toor2024          → root shell
 *   cat /root/flag.txt          → FLAG{pr1v_3sc_pwn3d}
 */
public class HashCrackChallenge implements ICTFChallenge {

    private static final String HASH =
        "$6$rounds=5000$salt99$Fp5MzL8QpxBfNkr.AbCdEfGhIj" +
        "KlMnOpQrStUvWxYz1234567890abcdef0000000000000000000";

    private static final String CRACKED_PASS = "toor2024";

    private enum Stage { INITIAL, CRACKED, ROOTED }
    private Stage stage = Stage.INITIAL;
    private boolean awaitingPassword = false;
    private boolean solved = false;

    @Override public String getTitle()      { return "TERMINAL-02 // HASH CRACK + PRIVESC"; }
    @Override public String getTargetInfo() { return "target: 10.10.10.22 (linux box)"; }
    @Override public String getPrompt() {
        if (stage == Stage.ROOTED) return "root@target:~# ";
        return "user@target:/tmp$ ";
    }

    @Override
    public TerminalLine[] getWelcomeLines() {
        return new TerminalLine[]{
            TerminalLine.warn("OBJECTIVE: Crack the leaked hash, escalate to root, read the flag."),
            TerminalLine.dim ("────────────────────────────────────────────────────────────"),
            TerminalLine.out ("You have low-priv shell access. A misconfigured backup"),
            TerminalLine.out ("left a copy of /etc/shadow in /tmp."),
            TerminalLine.dim ("Hint: list the directory first."),
        };
    }

    @Override
    public TerminalLine[] processInput(String raw) {
        String cmd = raw.trim();
        String lo  = cmd.toLowerCase();

        // Password prompt mode
        if (awaitingPassword) {
            awaitingPassword = false;
            if (cmd.equals(CRACKED_PASS)) {
                stage = Stage.ROOTED;
                return new TerminalLine[]{
                    TerminalLine.ok  ("Password accepted."),
                    TerminalLine.ok  ("root@target:~# "),
                    TerminalLine.warn("You are now root!"),
                };
            }
            return new TerminalLine[]{ TerminalLine.err("su: Authentication failure") };
        }

        if (lo.equals("ls") || lo.equals("ls -la")) return lsTmp();
        if (lo.startsWith("cat readme")) return catReadme();
        if (lo.startsWith("cat shadow.bak")) return catShadow();
        if (lo.startsWith("cat /root/flag") || lo.startsWith("cat flag")) {
            if (stage == Stage.ROOTED) return catFlag();
            return new TerminalLine[]{ TerminalLine.err("cat: /root/flag.txt: Permission denied") };
        }
        if (lo.startsWith("cat /etc/shadow")) {
            if (stage == Stage.ROOTED)
                return new TerminalLine[]{ TerminalLine.out("root:" + HASH + ":19999:0:99999:7:::") };
            return new TerminalLine[]{ TerminalLine.err("cat: /etc/shadow: Permission denied") };
        }
        if (lo.startsWith("hashcat") && lo.contains("--help"))  return hashcatHelp();
        if (lo.startsWith("hashcat") && lo.contains("-m 1800")) return hashcatCrack();
        if (lo.startsWith("hashcat") && lo.contains(HASH.substring(0,10))) return hashcatNeedsMode();
        if (lo.startsWith("hashcat")) return hashcatNeedsMode();
        if (lo.startsWith("john")) return johnInfo();
        if (lo.startsWith("su root") || lo.equals("su")) {
            if (stage != Stage.CRACKED && stage != Stage.ROOTED)
                return new TerminalLine[]{ TerminalLine.err("su: Authentication failure  (crack the hash first)") };
            if (stage == Stage.ROOTED)
                return new TerminalLine[]{ TerminalLine.warn("Already root.") };
            // check inline password
            if (lo.contains(CRACKED_PASS)) { stage = Stage.ROOTED;
                return new TerminalLine[]{
                    TerminalLine.ok ("Password accepted."),
                    TerminalLine.warn("You are now root!"),
                }; }
            awaitingPassword = true;
            return new TerminalLine[]{ TerminalLine.out("Password: ") };
        }
        if (lo.startsWith("whoami")) return new TerminalLine[]{ TerminalLine.out(stage==Stage.ROOTED?"root":"user") };
        if (lo.startsWith("id"))     return new TerminalLine[]{ TerminalLine.out(stage==Stage.ROOTED?"uid=0(root) gid=0(root) groups=0(root)":"uid=1001(user) gid=1001(user) groups=1001(user)") };
        if (lo.equals("pwd"))        return new TerminalLine[]{ TerminalLine.out(stage==Stage.ROOTED?"/root":"/tmp") };
        if (lo.equals("help") || lo.equals("?")) return help();
        if (lo.equals("clear"))       return new TerminalLine[]{};
        String c = cmd.split("\\s+")[0];
        return new TerminalLine[]{ TerminalLine.err("bash: " + c + ": command not found") };
    }

    @Override public boolean isSolved() { return solved; }
    @Override public void reset() { stage = Stage.INITIAL; awaitingPassword = false; solved = false; }

    // ── Sub-responses ────────────────────────────────────────────────────────

    private TerminalLine[] lsTmp() {
        return new TerminalLine[]{
            TerminalLine.out("shadow.bak  readme.txt  .bash_history"),
        };
    }

    private TerminalLine[] catReadme() {
        return new TerminalLine[]{
            TerminalLine.out("# Backup note — 2024-10-14"),
            TerminalLine.out("Backing up shadow to /tmp for the migration script."),
            TerminalLine.warn("TODO: delete this before going live. — sysadmin"),
        };
    }

    private TerminalLine[] catShadow() {
        return new TerminalLine[]{
            TerminalLine.out("root:" + HASH + ":19999:0:99999:7:::"),
            TerminalLine.dim("daemon:*:18858:0:99999:7:::"),
            TerminalLine.dim("user:$6$abc:19999:0:99999:7:::"),
        };
    }

    private TerminalLine[] hashcatHelp() {
        return new TerminalLine[]{
            TerminalLine.info("hashcat (v6.2.6) — usage:"),
            TerminalLine.out ("  hashcat -m <mode> <hashfile> <wordlist>"),
            TerminalLine.out ("  -m 0     MD5"),
            TerminalLine.out ("  -m 1000  NTLM"),
            TerminalLine.warn("  -m 1800  sha512crypt (Linux shadow)   ← what you need"),
            TerminalLine.out ("  example: hashcat -m 1800 shadow.bak /usr/share/wordlists/rockyou.txt"),
        };
    }

    private TerminalLine[] hashcatNeedsMode() {
        return new TerminalLine[]{
            TerminalLine.err ("hashcat: Need to specify hash type with -m"),
            TerminalLine.warn("Hint: Linux shadow hashes are sha512crypt  (-m 1800)"),
        };
    }

    private TerminalLine[] hashcatCrack() {
        if (stage == Stage.INITIAL) stage = Stage.CRACKED;
        return new TerminalLine[]{
            TerminalLine.dim ("hashcat (v6.2.6) starting..."),
            TerminalLine.dim ("Session........: hashcat"),
            TerminalLine.dim ("Status.........: Running"),
            TerminalLine.out ("Dictionary......: /usr/share/wordlists/rockyou.txt"),
            TerminalLine.dim ("[....................]  2% — 14,200 H/s"),
            TerminalLine.dim ("[#...................]  18% — 22,400 H/s"),
            TerminalLine.dim ("[####................]  43% — 31,100 H/s"),
            TerminalLine.dim ("[########............]  71% — 33,800 H/s"),
            TerminalLine.dim ("[############........]  89% — 34,200 H/s"),
            TerminalLine.ok  ("[####################]  100% — Cracked!"),
            TerminalLine.blank(),
            TerminalLine.ok  (HASH + ":" + CRACKED_PASS),
            TerminalLine.blank(),
            TerminalLine.warn("Password found: " + CRACKED_PASS),
            TerminalLine.dim ("Session..........: hashcat"),
            TerminalLine.dim ("Status...........: Cracked"),
            TerminalLine.dim ("Recovered........: 1/1 (100.00%) Digests"),
            TerminalLine.blank(),
            TerminalLine.info("Now escalate: su root  (use the cracked password)"),
        };
    }

    private TerminalLine[] johnInfo() {
        return new TerminalLine[]{
            TerminalLine.warn("john is not installed. Try hashcat instead."),
            TerminalLine.dim ("hashcat --help for usage."),
        };
    }

    private TerminalLine[] catFlag() {
        solved = true;
        return new TerminalLine[]{
            TerminalLine.blank(),
            TerminalLine.ok  ("██████████████████████████████████████"),
            TerminalLine.ok  ("  FLAG{pr1v_3sc_pwn3d_2024}"),
            TerminalLine.ok  ("██████████████████████████████████████"),
            TerminalLine.blank(),
            TerminalLine.warn(">>> PRIVILEGE ESCALATION SUCCESS — KEY EXTRACTED <<<"),
        };
    }

    private TerminalLine[] help() {
        return new TerminalLine[]{
            TerminalLine.info("Useful commands:"),
            TerminalLine.out ("  ls / cat <file>           — explore the filesystem"),
            TerminalLine.out ("  hashcat --help            — show hashcat usage"),
            TerminalLine.out ("  hashcat -m 1800 shadow.bak /usr/share/wordlists/rockyou.txt"),
            TerminalLine.out ("  su root                   — switch to root"),
            TerminalLine.out ("  cat /root/flag.txt        — read the flag (root only)"),
        };
    }
}
